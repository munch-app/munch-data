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
import munch.data.place.graph.matcher.MatcherManager;
import munch.restful.core.JsonUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 9/10/2017
 * Time: 2:49 AM
 * Project: munch-data
 */
@Singleton
public final class ElasticClient {
    private static final ObjectMapper objectMapper = JsonUtils.objectMapper;

    private final JestClient client;
    private final Set<String> requiredFields;

    @Inject
    public ElasticClient(@Named("munch.data.place.jest") JestClient client, MatcherManager matcherManager) {
        this.client = client;
        this.requiredFields = matcherManager.getRequiredFields();
    }

    public void put(long cycleNo, CorpusData corpusData, PlaceTree placeTree) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("cycleNo", cycleNo);
        node.put("treeSize", placeTree == null ? 0 : placeTree.getSize());
        node.set("fields", toNodes(corpusData.getFields()));

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
                data.setFields(toFields(hit.path("_source").path("fields")));
                dataList.add(data);
            }
            return dataList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CorpusData> search(PlaceTree tree, JsonNode... filters) {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        arrayNode.add(filterRange("treeSize", "lte", tree.getSize()));

        for (JsonNode filter : filters) {
            arrayNode.add(filter);
        }

        return search(createQuery(0, 1000, arrayNode));
    }

    private List<CorpusData.Field> toFields(JsonNode fields) {
        List<CorpusData.Field> fieldList = new ArrayList<>();
        fields.fields().forEachRemaining(entry -> {
            String key = entry.getKey().replace('_', '.');
            if (entry.getValue().isArray()) {
                for (JsonNode node : entry.getValue()) {
                    fieldList.add(new CorpusData.Field(key, node.asText()));
                }
            } else {
                fieldList.add(new CorpusData.Field(key, entry.getValue().asText()));
            }
        });

        return fieldList;
    }

    private JsonNode toNodes(List<CorpusData.Field> fields) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        fields.stream()
                .filter(field -> requiredFields.contains(field.getKey()))
                .collect(Collectors.toMap(CorpusData.Field::getKey, CorpusData.Field::getValue))
                .forEach((key, values) -> {
                    objectNode.set(key.replace('.', '_'), objectMapper.valueToTree(values));
                });

        return objectNode;
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
                .put(fieldName.replace('.', '_'), latLng);
        return filter;
    }

    public static JsonNode filterRange(String fieldName, String comparator, long value) {
        ObjectNode filter = objectMapper.createObjectNode();
        filter.putObject("range")
                .putObject(fieldName.replace('.', '_'))
                .put(comparator, value);
        return filter;
    }

    public static JsonNode filterTerm(String fieldName, String value) {
        ObjectNode filter = objectMapper.createObjectNode();
        filter.putObject("term")
                .put(fieldName, value);
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
