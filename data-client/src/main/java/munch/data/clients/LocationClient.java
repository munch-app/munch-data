package munch.data.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import munch.data.elastic.ElasticClient;
import munch.data.elastic.ElasticIndex;
import munch.data.elastic.ElasticMarshaller;
import munch.data.structure.Location;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 10/10/2017
 * Time: 2:16 AM
 * Project: munch-data
 */
@Singleton
public class LocationClient extends AbstractClient {
    private final ElasticIndex elasticIndex;
    private final ElasticClient elasticClient;
    private final ElasticMarshaller marshaller;

    @Inject
    public LocationClient(ElasticIndex elasticIndex, ElasticClient elasticClient, ElasticMarshaller marshaller) {
        this.elasticIndex = elasticIndex;
        this.elasticClient = elasticClient;
        this.marshaller = marshaller;
    }

    /**
     * @param text text
     * @param size size of location to suggest
     * @return list of Location
     */
    public List<Location> suggest(String text, int size) {
        JsonNode results = elasticClient.suggest("Location", text, null, size);
        return marshaller.deserializeList(results);
    }

    /**
     * @param text text
     * @param size size of location to search
     * @return list of Location
     */
    public List<Location> search(String text, int size) {
        ObjectNode bool = objectMapper.createObjectNode();
        bool.set("must", must(text));

        JsonNode result = elasticClient.postBoolSearch("Location", 0, size, bool, null);
        JsonNode hits = result.path("hits");

        return marshaller.deserializeList(hits.path("hits"));
    }

    public Location get(String id) {
        return elasticIndex.get("Location", id);
    }


    public void put(Location location) {
        elasticIndex.put(location);
    }

    public void delete(String id) {
        elasticIndex.delete("Location", id);
    }

    /**
     * Search with text on name
     *
     * @param query query string
     * @return JsonNode must filter
     */
    private static JsonNode must(String query) {
        ObjectNode root = objectMapper.createObjectNode();

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
}
