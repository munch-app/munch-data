package munch.place.elastic;

import catalyst.utils.iterators.PaginationIterator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.searchbox.client.JestClient;
import io.searchbox.core.DeleteByQuery;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import munch.restful.core.JsonUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 9/10/2017
 * Time: 2:49 AM
 * Project: munch-data
 */
@Singleton
public class ElasticClient {

    private final JestClient client;
    private final ObjectMapper mapper = JsonUtils.objectMapper;

    @Inject
    public ElasticClient(JestClient client) {
        this.client = client;
    }

    public void put(long cycleNo, PartialPlace place) {
        ObjectNode node = mapper.createObjectNode();
        node.put("cycleNo", cycleNo);

        node.set("name", mapper.valueToTree(place.getName()));
        node.set("postal", mapper.valueToTree(place.getPostal()));

        try {
            String json = mapper.writeValueAsString(node);
            client.execute(new Index.Builder(json)
                    .index("corpus")
                    .type(place.getCorpusName())
                    .id(place.getCorpusKey())
                    .build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(long cycleNo) {
        ObjectNode root = mapper.createObjectNode();
        root.putObject("query")
                .putObject("range")
                .putObject("cycleNo")
                .put("lt", cycleNo);

        try {
            String json = mapper.writeValueAsString(root);
            client.execute(new DeleteByQuery.Builder(json)
                    .addIndex("corpus")
                    .setParameter("conflicts", "proceed")
                    .build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Iterator<PartialPlace> search(String postal) {
        return new PaginationIterator<>(from -> search(postal, from, 50));
    }

    public List<PartialPlace> search(String postal, int from, int size) {
        ObjectNode root = mapper.createObjectNode();
        root.put("from", from)
                .put("size", size)
                .putObject("query")
                .putObject("must")
                .putObject("match")
                .put("postal", postal);

        try {
            Search.Builder builder = new Search.Builder(mapper.writeValueAsString(root))
                    .addIndex("corpus");
            JsonNode result = mapper.readTree(client.execute(builder.build()).getJsonString());
            return deserializeList(result.path("hits").path("hits"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<PartialPlace> deserializeList(JsonNode results) {
        if (results.isMissingNode()) return Collections.emptyList();
        List<PartialPlace> list = new ArrayList<>();
        for (JsonNode result : results) {
            PartialPlace place = new PartialPlace();
            place.setCorpusName(result.path("_index").asText());
            place.setCorpusKey(result.path("_id").asText());
            place.setName(deserializeStrings(result.path("name")));
            place.setPostal(deserializeStrings(result.path("postal")));
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
