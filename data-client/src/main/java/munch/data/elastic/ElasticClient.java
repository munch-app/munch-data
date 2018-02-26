package munch.data.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Singleton;
import io.searchbox.client.JestClient;
import io.searchbox.core.Count;
import io.searchbox.core.MultiSearch;
import io.searchbox.core.Search;
import munch.data.exceptions.ElasticException;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
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
    private static final ObjectMapper mapper = JsonUtils.objectMapper;

    @Inject
    public ElasticClient(JestClient client) {
        this.client = client;
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
                .put("fuzzy", true)
                .put("size", size);
        ObjectNode contexts = completion.putObject("contexts");

        // Context: LatLng
        if (StringUtils.isNotBlank(latLng)) {
            String[] lls = latLng.split(",");
            final double lat = Double.parseDouble(lls[0].trim());
            final double lng = Double.parseDouble(lls[1].trim());

            ArrayNode latLngArray = contexts.putArray("latLng");
            latLngArray.addObject()
                    .put("precision", 3)
                    .putObject("context")
                    .put("lat", lat)
                    .put("lon", lng);

            latLngArray.addObject()
                    .put("precision", 6)
                    .put("boost", 1.05)
                    .putObject("context")
                    .put("lat", lat)
                    .put("lon", lng);
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
    public JsonNode search(List<String> types, String text, int from, int size) {
        Search search = createSearch(types, text, from, size);

        try {
            JsonNode result = mapper.readTree(client.execute(search).getJsonString());
            JsonNode hits = result.path("hits");
            return hits.path("hits");
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
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
                    .addIndex(ElasticMapping.INDEX_NAME);

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
                    .addIndex(ElasticMapping.INDEX_NAME);

            return mapper.readTree(client.execute(builder.build())
                    .getJsonString());
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }

    /**
     * @param searches multi searches to perform
     * @return List of search result
     */
    public List<JsonNode> postMultiSearch(List<Search> searches) {
        if (searches.isEmpty()) return List.of();

        try {
            MultiSearch.Builder builder = new MultiSearch.Builder(searches);
            JsonNode rootResponse = mapper.readTree(client.execute(builder.build()).getJsonString());
            parseResponse(rootResponse);

            List<JsonNode> responses = new ArrayList<>();
            for (JsonNode node : rootResponse.path("responses")) {
                parseResponse(node);
                responses.add(node.path("hits").path("hits"));
            }
            return responses;
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }

    /**
     * @param types types to query against
     * @param text  text to query
     * @param from  from
     * @param size  size
     * @return Search object for elasticsearch
     */
    public static Search createSearch(List<String> types, String text, int from, int size) {
        ObjectNode bool = mapper.createObjectNode();
        bool.set("must", must(text));
        bool.set("filter", filter(types));

        ObjectNode root = mapper.createObjectNode();
        root.put("from", from);
        root.put("size", size);
        root.putObject("query").set("bool", bool);

        Search.Builder builder = new Search.Builder(JsonUtils.toString(root))
                .addIndex(ElasticMapping.INDEX_NAME);
        return builder.build();
    }

    /**
     * @param response response
     * @throws ElasticException if error exists
     */
    public static void parseResponse(JsonNode response) throws ElasticException {
        if (!response.has("status")) return;
        int status = response.path("status").asInt();
        if (status == 200) return;

        String type = response.path("type").asText();
        String reason = response.path("reason").asText();
        throw new ElasticException(status, type + ": " + reason);
    }

    /**
     * Search with text on name
     *
     * @param query query string
     * @return JsonNode must filter
     */
    static JsonNode must(String query) {
        ObjectNode root = mapper.createObjectNode();

        // Match all if query is blank
        if (StringUtils.isBlank(query)) {
            root.putObject("match_all");
            return root;
        }

        // Match name if got query
        ObjectNode match = root.putObject("match_phrase_prefix");
        match.put("name", query);
        return root;
    }

    static JsonNode mustFuzzy(String query) {
        ObjectNode root = mapper.createObjectNode();

        // Match all if query is blank
        if (StringUtils.isBlank(query)) {
            root.putObject("match_all");
            return root;
        }

        // Match name if got query
        ObjectNode match = root.putObject("fuzzy");
        match.put("name", query);
        return root;
    }

    /**
     * @param types types to filter by only
     * @return JsonNode filter
     */
    static JsonNode filter(List<String> types) {
        ArrayNode filterArray = mapper.createArrayNode();
        if (types == null) return filterArray;

        // Filtered Type
        for (String type : types) {
            filterArray.addObject()
                    .putObject("term")
                    .put("dataType", type);
        }
        return filterArray;
    }
}
