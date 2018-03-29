package munch.data.place.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import corpus.data.CorpusData;
import io.searchbox.client.JestClient;
import io.searchbox.core.DeleteByQuery;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import munch.data.place.graph.matcher.MatcherManager;
import munch.restful.core.JsonUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 9/10/2017
 * Time: 2:49 AM
 * Project: munch-data
 */
@Singleton
public class ElasticClient {
    protected static final ObjectMapper mapper = JsonUtils.objectMapper;

    private final JestClient client;
    private final Set<String> requiredFields;

    @Inject
    public ElasticClient(@Named("munch.data.place.jest") JestClient client, MatcherManager matcherManager) {
        this.client = client;
        this.requiredFields = matcherManager.getRequiredFields();
    }

    public void put(long cycleNo, CorpusData corpusData) {
        ObjectNode node = mapper.createObjectNode();
        node.put("cycleNo", cycleNo);

        ObjectNode fieldNode = node.putObject("field");
        for (CorpusData.Field field : corpusData.getFields()) {

        }

        node.set("name", mapper.valueToTree(place.getName()));
        node.set("postal", mapper.valueToTree(place.getPostal()));
        node.put("latLng", place.getLatLng());

        try {
            String json = mapper.writeValueAsString(node);
            client.execute(new Index.Builder(json).index("graph")
                    .type(corpusData.getCorpusName()).id(corpusData.getCorpusKey())
                    .build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteBefore(long cycleNo) {
        ObjectNode root = mapper.createObjectNode();
        root.putObject("query")
                .putObject("range")
                .putObject("cycleNo")
                .put("lt", cycleNo);

        try {
            String json = mapper.writeValueAsString(root);
            client.execute(new DeleteByQuery.Builder(json)
                    .addIndex("graph")
                    .setParameter("conflicts", "proceed")
                    .build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected List<CorpusData> search(JsonNode node) {
        try {
            Search.Builder builder = new Search.Builder(mapper.writeValueAsString(node)).addIndex("graph");
            JsonNode result = mapper.readTree(client.execute(builder.build()).getJsonString());
            return deserializeList(result.path("hits").path("hits"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<CorpusData> deserializeList(JsonNode results) {
        if (results.isMissingNode()) return List.of();
        List<CorpusData> list = new ArrayList<>();

        for (JsonNode result : results) {
            CorpusData place = new CorpusData();
            place.setCorpusName(result.path("_type").asText());
            place.setCorpusKey(result.path("_id").asText());

            JsonNode source = result.path("_source");

            place.setName(deserializeStrings(source.path("name")));
            place.setPostal(deserializeStrings(source.path("postal")));
            place.setLatLng(source.path("latLng").asText());
            list.add(place);
        }
        return list;
    }

    private static List<CorpusData.Field> toFields() {

    }

    private static List<CorpusData.Field> toNodes() {

    }
}
