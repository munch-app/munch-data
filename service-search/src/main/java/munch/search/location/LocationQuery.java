package munch.search.location;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by: Fuxing
 * Date: 8/7/2017
 * Time: 4:55 PM
 * Project: munch-core
 */
@Singleton
public final class LocationQuery {
    private final ObjectMapper mapper;

    @Inject
    public LocationQuery(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * @param lat latitude
     * @param lng longitude
     * @return bool reverse query
     */
    public JsonNode reverse(double lat, double lng) {
        ObjectNode bool = mapper.createObjectNode();
        bool.set("must", must(null));
        bool.set("filter", filter(lat, lng));
        return bool;
    }

    /**
     * @param text text query
     * @return bool search query
     */
    public JsonNode search(String text) {
        return mapper.createObjectNode().set("must", must(text));
    }

    /**
     * @param query query, can be null
     * @return { "match_all": {} } OR { "match": { "name": query }}
     */
    private JsonNode must(String query) {
        ObjectNode root = mapper.createObjectNode();

        // Match all if query is blank
        if (StringUtils.isBlank(query)) {
            root.putObject("match_all");
            return root;
        }

        // Match name if got query
        root.putObject("match").put("name", query);
        return root;
    }

    /**
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-geo-shape-query.html
     *
     * @param lat latitude
     * @param lng longitude
     * @return { "geo_shape": { "points": { "shape": { "type": "point", "coordinates": [lat, lng] }}}}
     */
    private JsonNode filter(double lat, double lng) {
        ObjectNode root = mapper.createObjectNode();
        root.putObject("geo_shape")
                .putObject("location.polygon")
                .putObject("shape")
                .put("type", "point")
                .putArray("coordinates").add(lng).add(lat);
        return root;
    }
}
