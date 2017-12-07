package munch.data.place.elastic;

import catalyst.utils.iterators.PaginationIterator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 5/12/2017
 * Time: 10:55 PM
 * Project: munch-data
 */
@Singleton
public final class SpatialClient extends ElasticClient {

    @Inject
    public SpatialClient() {
        super("spatial");
    }

    public Iterator<ElasticPlace> search(String latLng, double metres) {
        return new PaginationIterator<>(from -> search(latLng, metres, from, 50));
    }

    public List<ElasticPlace> search(String latLng, double metres, int from, int size) {
        ObjectNode root = mapper.createObjectNode();
        root.put("from", from)
                .put("size", size)
                .putObject("query")
                .putObject("bool")
                .putArray("filter")
                .add(filterDistance(latLng, metres));

        return search(root);
    }

    /**
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-geo-distance-query.html
     *
     * @param latLng latLng center
     * @param metres metres in distance
     * @return JsonNode = { "geo_distance": { "distance": "1km", "location.latLng": "-1,2"}}
     */
    private JsonNode filterDistance(String latLng, double metres) {
        ObjectNode filter = mapper.createObjectNode();
        filter.putObject("geo_distance")
                .put("distance", metres + "m")
                .put("latLng", latLng);
        return filter;
    }
}
