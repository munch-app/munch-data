package munch.data.place.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import corpus.data.CorpusData;
import io.searchbox.client.JestClient;
import io.searchbox.core.DeleteByQuery;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import munch.data.place.graph.PlaceTree;
import munch.restful.core.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 9/10/2017
 * Time: 2:49 AM
 * Project: munch-data
 */
@Singleton
public final class ElasticClient {
    private static final Logger logger = LoggerFactory.getLogger(ElasticClient.class);
    private static final ObjectMapper objectMapper = JsonUtils.objectMapper;

    private final JestClient client;
    private final ElasticMarshaller marshaller;

    @Inject
    public ElasticClient(@Named("munch.data.place.jest") JestClient client, ElasticMarshaller marshaller) {
        this.client = client;
        this.marshaller = marshaller;
    }

    public void put(long cycleNo, CorpusData corpusData, PlaceTree placeTree) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("cycleNo", cycleNo);
        node.put("treeSize", placeTree == null ? 0 : placeTree.getSize());
        node.set("fields", marshaller.toNodes(corpusData.getFields()));

        try {
            String json = objectMapper.writeValueAsString(node);
            client.execute(new Index.Builder(json).index("graph")
                    .type(corpusData.getCorpusName()).id(corpusData.getCorpusKey())
                    .build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteBefore(long cycleNo) {
        ObjectNode root = objectMapper.createObjectNode();
        root.putObject("query")
                .putObject("range")
                .putObject("cycleNo")
                .put("lt", cycleNo);

        try {
            String json = objectMapper.writeValueAsString(root);
            client.execute(new DeleteByQuery.Builder(json)
                    .addIndex("graph")
                    .setParameter("conflicts", "proceed")
                    .build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<CorpusData> search(JsonNode node) {
        try {
            Search.Builder builder = new Search.Builder(objectMapper.writeValueAsString(node)).addIndex("graph");
            JsonNode result = objectMapper.readTree(client.execute(builder.build()).getJsonString());
            JsonNode hits = result.path("hits").path("hits");

            if (hits.isMissingNode()) return List.of();

            List<CorpusData> dataList = new ArrayList<>();
            for (JsonNode hit : hits) {
                String corpusName = hit.path("_type").asText();
                String corpusKey = hit.path("_id").asText();
                long cycleNo = hit.path("_source").path("cycleNo").asLong();

                CorpusData data = new CorpusData(corpusName, corpusKey, cycleNo);
                data.setFields(marshaller.toFields(hit.path("_source").path("fields")));
                dataList.add(data);
            }
            return dataList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CorpusData> search(PlaceTree tree, JsonNode... filters) {
        int size = tree.getSize();

        if (size > 1000) {
            logger.warn("PlaceTree size more than 1000, actual: {}, id: {}", tree.getSize(), tree.getCorpusData().getCatalystId());
        }
        ArrayNode arrayNode = objectMapper.createArrayNode();

        ObjectNode treeSizeRange = objectMapper.createObjectNode();
        treeSizeRange.putObject("range").putObject("treeSize").put("lte", size);
        arrayNode.add(treeSizeRange);

        for (JsonNode filter : filters) {
            arrayNode.add(filter);
        }

        return search(createQuery(0, 200, arrayNode));
    }

    /**
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-geo-distance-query.html
     *
     * @param latLng latLng center
     * @param metres metres in distance
     * @return JsonNode = { "geo_distance": { "distance": "1km", "location.latLng": "-1,2"}}
     */
    public static JsonNode filterDistance(String fieldName, String latLng, double metres) {
        ObjectNode filter = objectMapper.createObjectNode();
        filter.putObject("geo_distance")
                .put("distance", metres + "m")
                .put("fields." +fieldName, latLng);
        return filter;
    }

    public static JsonNode filterRange(String fieldName, String comparator, long value) {
        ObjectNode filter = objectMapper.createObjectNode();
        filter.putObject("range")
                .putObject("fields." +fieldName)
                .put(comparator, value);
        return filter;
    }

    public static JsonNode filterTerm(String fieldName, String value) {
        ObjectNode filter = objectMapper.createObjectNode();
        filter.putObject("term").put("fields." +fieldName, value);
        return filter;
    }

    public static JsonNode createQuery(int from, int size, ArrayNode filters) {
        ObjectNode root = objectMapper.createObjectNode();

        root.put("from", from)
                .put("size", size)
                .putObject("query")
                .putObject("bool")
                .set("filter", filters);
        return root;
    }
}
