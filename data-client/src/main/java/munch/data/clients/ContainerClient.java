package munch.data.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import munch.data.elastic.ElasticClient;
import munch.data.elastic.ElasticIndex;
import munch.data.elastic.ElasticMarshaller;
import munch.data.elastic.query.SortQuery;
import munch.data.exceptions.ElasticException;
import munch.data.structure.Container;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 10/12/2017
 * Time: 10:22 AM
 * Project: munch-data
 */
@Singleton
public class ContainerClient extends AbstractClient {
    private final ElasticIndex elasticIndex;
    private final ElasticClient elasticClient;
    private final ElasticMarshaller marshaller;

    @Inject
    public ContainerClient(ElasticIndex elasticIndex, ElasticClient elasticClient, ElasticMarshaller marshaller) {
        this.elasticIndex = elasticIndex;
        this.elasticClient = elasticClient;
        this.marshaller = marshaller;
    }

    /**
     * @param latLng latLng = latitude,longitude
     * @param radius radius in metres
     * @param size   size of location to search
     * @return list of Location
     */
    public List<Container> search(String latLng, double radius, int size) {
        ObjectNode bool = objectMapper.createObjectNode();
        bool.set("filter", filter(latLng, radius));

        JsonNode result = elasticClient.postBoolSearch(0, size, bool, sort(latLng));
        JsonNode hits = result.path("hits");
        return marshaller.deserializeList(hits.path("hits"));
    }

    public Container get(String id) throws ElasticException {
        return elasticIndex.get("Container", id);
    }

    public void put(Container container) throws ElasticException {
        elasticIndex.put(container);
    }

    public void delete(String id) throws ElasticException {
        elasticIndex.delete("Container", id);
    }

    /**
     * Search with text on name
     * <p>
     * Filter to Container
     * Filter to count >= 5
     * Filter to geo distance
     *
     * @param latLng latLng = latitude,longitude
     * @param radius radius in metres
     * @return JsonNode must filter
     */
    private static JsonNode filter(String latLng, double radius) {
        Objects.requireNonNull(latLng);

        ArrayNode filterArray = objectMapper.createArrayNode();
        filterArray.addObject()
                .putObject("term")
                .put("dataType", "Container");

        filterArray.addObject()
                .putObject("range")
                .putObject("count")
                .put("gte", 5);

        filterArray.addObject()
                .putObject("geo_distance")
                .put("distance", radius + "m")
                .put("location.latLng", latLng);

        return filterArray;
    }

    /**
     * Sort by ranking desc first
     * Then sort by distance
     *
     * @param latLng latLng for 2nd sort by
     * @return default sort for Container search
     */
    private static JsonNode sort(String latLng) {
        ArrayNode sortArray = objectMapper.createArrayNode();
        sortArray.add(SortQuery.sortField("count", "desc"));
        sortArray.add(SortQuery.sortDistance(latLng));
        return sortArray;
    }
}
