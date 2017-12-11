package munch.data.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Singleton;
import io.searchbox.client.JestClient;
import io.searchbox.core.Count;
import io.searchbox.core.Search;
import munch.data.exceptions.ElasticException;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;

/**
 * Created By: Fuxing Loh
 * Date: 22/3/2017
 * Time: 9:22 PM
 * Project: munch-core
 */
@Singleton
public final class ElasticClient {
    private final JestClient client;
    private final ObjectMapper mapper;

    @Inject
    public ElasticClient(JestClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    /**
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/search-suggesters-completion.html#querying
     *
     * @param query  query string
     * @param latLng nullable latLng
     * @param size   size of suggestion of place
     * @return options array nodes containing the results
     */
    public JsonNode suggest(@Nullable String type, String query, @Nullable String latLng, int size) {
        ObjectNode completion = mapper.createObjectNode()
                .put("field", "suggest")
                .put("size", size);
        ObjectNode contexts = completion.putObject("contexts");

        // Context: LatLng
        if (StringUtils.isNotBlank(latLng)) {
            String[] lls = latLng.split(",");

            contexts.putObject("latLng")
                    .put("lat", Double.parseDouble(lls[0].trim()))
                    .put("lon", Double.parseDouble(lls[1].trim()));
        }

        // Context: Type
        if (type != null) {
            contexts.set("dataType", mapper.createArrayNode().add(type));
        }

        ObjectNode root = mapper.createObjectNode();
        root.putObject("suggest")
                .putObject("suggestions")
                .put("prefix", StringUtils.lowerCase(query))
                .set("completion", completion);

        // Query, parse and return options array node
        return postSearch(root)
                .path("suggest")
                .path("suggestions")
                .path(0)
                .path("options");
    }

    /**
     * @param type      type to focus
     * @param from      page from
     * @param size      page size
     * @param boolQuery bool query node
     * @return JsonNode
     * @throws IOException exception
     */
    public JsonNode postBoolSearch(int from, int size, JsonNode boolQuery) {
        return postBoolSearch(from, size, boolQuery, null);
    }

    /**
     * @param from      page from
     * @param size      page size
     * @param boolQuery bool query node
     * @param sort      sort nodes
     * @return JsonNode
     */
    public JsonNode postBoolSearch(int from, int size, JsonNode boolQuery, @Nullable JsonNode sort) {
        ObjectNode root = mapper.createObjectNode();
        root.put("from", from);
        root.put("size", size);
        root.putObject("query").set("bool", boolQuery);
        if (sort != null) root.set("sort", sort);

        return postSearch(root);
    }

    public long postBoolCount(JsonNode boolQuery) {
        ObjectNode root = mapper.createObjectNode();
        root.putObject("query").set("bool", boolQuery);

        try {
            Count.Builder builder = new Count.Builder()
                    .query(mapper.writeValueAsString(root))
                    .addIndex("munch");

            Double count = client.execute(builder.build()).getCount();
            if (count == null) return 0;
            return count.longValue();
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }

    /**
     * @param type type to focus
     * @param node search node
     * @return root node
     */
    public JsonNode postSearch(JsonNode node) {
        try {
            Search.Builder builder = new Search.Builder(mapper.writeValueAsString(node))
                    .addIndex("munch");

            return mapper.readTree(client.execute(builder.build())
                    .getJsonString());
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }
}
