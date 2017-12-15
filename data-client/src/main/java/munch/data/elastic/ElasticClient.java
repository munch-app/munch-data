package munch.data.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import java.util.List;

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
     * Basic suggest search via suggest data type
     *
     * @param type   type to filter
     * @param query  query string
     * @param latLng nullable latLng
     * @param size   size of suggestion of place
     * @return options array nodes containing the results
     */
    public JsonNode suggest(String type, String query, @Nullable String latLng, int size) {
        return suggest(type == null ? null : List.of(type), query, latLng, size);
    }

    /**
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/search-suggesters-completion.html#querying
     * Basic suggest search via suggest data type
     *
     * @param types  types to filter
     * @param query  query string
     * @param latLng nullable latLng
     * @param size   size of suggestion of place
     * @return options array nodes containing the results
     */
    public JsonNode suggest(List<String> types, String query, @Nullable String latLng, int size) {
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
        if (types != null && !types.isEmpty()) {
            contexts.set("dataType", mapper.valueToTree(types));
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
     * @param text text
     * @param size size of location to search
     * @return list of hits
     */
    public JsonNode search(List<String> types, String text, int size) {
        ObjectNode bool = mapper.createObjectNode();
        bool.set("must", must(text));
        bool.set("filter", filter(types));

        JsonNode result = postBoolSearch(0, size, bool, null);
        JsonNode hits = result.path("hits");
        return hits.path("hits");
    }

    /**
     * Search with text on name
     *
     * @param query query string
     * @return JsonNode must filter
     */
    private JsonNode must(String query) {
        ObjectNode root = mapper.createObjectNode();

        // Match all if query is blank
        if (StringUtils.isBlank(query)) {
            root.putObject("match_all");
            return root;
        }

        // Match name if got query
        ObjectNode match = root.putObject("match");
        match.put("name", query);
        return root;
    }

    private JsonNode filter(List<String> types) {
        ArrayNode filterArray = mapper.createArrayNode();

        // Filtered Type
        if (types != null && !types.isEmpty()) {
            filterArray.addObject()
                    .putObject("term")
                    .set("dataType", mapper.valueToTree(types));
        }
        return filterArray;
    }

    /**
     * @param from      page from
     * @param size      page size
     * @param boolQuery bool query node
     * @return JsonNode
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
