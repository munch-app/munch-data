package munch.data.place.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.searchbox.client.JestClient;
import io.searchbox.core.DeleteByQuery;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import munch.restful.core.JsonUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 9/10/2017
 * Time: 2:49 AM
 * Project: munch-data
 */
public abstract class ElasticClient {
    protected static final ObjectMapper mapper = JsonUtils.objectMapper;

    protected final String index;
    private JestClient client;

    protected ElasticClient(String index) {
        this.index = index;
    }

    @Inject
    void inject(@Named("munch.data.place.jest") JestClient client) {
        this.client = client;
    }

    public void put(long cycleNo, ElasticPlace place) {
        ObjectNode node = mapper.createObjectNode();
        node.put("cycleNo", cycleNo);

        node.set("name", mapper.valueToTree(place.getName()));
        node.set("postal", mapper.valueToTree(place.getPostal()));
        node.put("latLng", place.getLatLng());

        try {
            String json = mapper.writeValueAsString(node);
            client.execute(new Index.Builder(json)
                    .index(index)
                    .type(place.getCorpusName())
                    .id(place.getCorpusKey())
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
                    .addIndex(index)
                    .setParameter("conflicts", "proceed")
                    .build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected List<ElasticPlace> search(JsonNode node) {
        try {
            Search.Builder builder = new Search.Builder(mapper.writeValueAsString(node))
                    .addIndex(index);
            JsonNode result = mapper.readTree(client.execute(builder.build()).getJsonString());
            return deserializeList(result.path("hits").path("hits"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<ElasticPlace> deserializeList(JsonNode results) {
        if (results.isMissingNode()) return Collections.emptyList();
        List<ElasticPlace> list = new ArrayList<>();
        for (JsonNode result : results) {
            ElasticPlace place = new ElasticPlace();
            place.setCorpusName(result.path("_type").asText());
            place.setCorpusKey(result.path("_id").asText());
            place.setName(deserializeStrings(result.path("_source").path("name")));
            place.setPostal(deserializeStrings(result.path("_source").path("postal")));
            list.add(place);
        }
        return list;
    }

    private static List<String> deserializeStrings(JsonNode results) {
        if (results.isArray()) {
            List<String> strings = new ArrayList<>();
            for (JsonNode result : results) {
                strings.add(result.asText());
            }
            return strings;
        } else if (results.isValueNode()) {
            return Collections.singletonList(results.asText());
        }
        return Collections.emptyList();
    }
}
