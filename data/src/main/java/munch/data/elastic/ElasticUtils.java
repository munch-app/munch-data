package munch.data.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import munch.data.ElasticObject;
import munch.data.location.Area;
import munch.data.location.Landmark;
import munch.data.place.Place;
import munch.data.tag.Tag;
import munch.restful.core.JsonUtils;

import java.util.*;

/**
 * Created by: Fuxing
 * Date: 3/6/18
 * Time: 3:46 PM
 * Project: munch-data
 */
public final class ElasticUtils {


    /**
     * @param results from es
     * @param <T>     deserialized type
     * @return deserialized type into a list
     */
    public static <T extends ElasticObject> List<T> deserializeList(JsonNode results) {
        if (results.isMissingNode()) return Collections.emptyList();

        List<T> list = new ArrayList<>();
        for (JsonNode result : results) list.add(deserialize(result));
        return list;
    }

    /**
     * @param node node to deserialize
     * @param <T>  deserialized type
     * @return deserialized type
     */
    @SuppressWarnings("unchecked")
    public static <T extends ElasticObject> T deserialize(JsonNode node) {
        JsonNode source = node.path("_source");
        switch (source.path("dataType").asText()) {
            case "Tag":
                return (T) JsonUtils.toObject(source, Tag.class);
            case "Landmark":
                return (T) JsonUtils.toObject(source, Landmark.class);
            case "Area":
                return (T) JsonUtils.toObject(source, Area.class);
            case "Place":
                return (T) JsonUtils.toObject(source, Place.class);
            default:
                return null;
        }
    }

    /**
     * Search with text on name
     *
     * @return JsonNode must filter
     */
    public static JsonNode mustMatchAll() {
        ObjectNode root = JsonUtils.createObjectNode();
        root.putObject("match_all");
        return root;
    }

    /**
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-geo-polygon-query.html
     *
     * @param name      name of term
     * @param pointList list of points to form a polygon
     * @return JsonNode = { "geo_polygon": { "location.latLng": { "points": ["-1,2", "-5,33" ...]}}}
     */
    public static JsonNode filterPolygon(String name, List<String> pointList) {
        ObjectNode filter = JsonUtils.createObjectNode();
        ArrayNode points = filter.putObject("geo_polygon")
                .putObject(name)
                .putArray("points");
        pointList.forEach(points::add);
        return filter;
    }

    /**
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-geo-distance-query.html
     *
     * @param name   name of term
     * @param latLng latLng center
     * @param metres metres in distance
     * @return JsonNode = { "geo_distance": { "distance": "1km", "location.latLng": "-1,2"}}
     */
    public static JsonNode filterDistance(String name, String latLng, double metres) {
        ObjectNode filter = JsonUtils.createObjectNode();
        filter.putObject("geo_distance")
                .put("distance", metres + "m")
                .put(name, latLng);
        return filter;
    }

    /**
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-geo-shape-query.html
     *
     * @param name   name of term
     * @param latLng point within
     * @return {"geo_shape": { "location": {
     * "shape": { "type": "point", "coordinates" : [13.0, 53.0]},
     * "relation": "within"
     * }}}
     */
    public static JsonNode filterWithinPoint(String name, String latLng) {
        ObjectNode filter = JsonUtils.createObjectNode();
        String[] split = latLng.split(",");

        filter.putObject("geo_shape")
                .putObject(name)
                .put("relation", "within")
                .putObject("shape")
                .put("type", "point")
                .putArray("coordinates")
                .add(Double.parseDouble(split[1]))
                .add(Double.parseDouble(split[0]));
        return filter;
    }

    /**
     * @param name name of term
     * @param text text of term
     * @return JsonNode =  { "term" : { "name" : "text" } }
     */
    public static JsonNode filterTerm(String name, String text) {
        ObjectNode filter = JsonUtils.createObjectNode();
        filter.putObject("term").put(name, text);
        return filter;
    }

    public static JsonNode filterTerm(String name, boolean value) {
        ObjectNode filter = JsonUtils.createObjectNode();
        filter.putObject("term").put(name, value);
        return filter;
    }

    /**
     * @param name  name of terms
     * @param texts texts of terms
     * @return JsonNode =  { "terms" : { "name" : "text" } }
     */
    public static JsonNode filterTerms(String name, Collection<String> texts) {
        ObjectNode filter = JsonUtils.createObjectNode();
        ArrayNode terms = filter.putObject("terms").putArray(name);
        for (String text : texts) {
            terms.add(text.toLowerCase());
        }
        return filter;
    }

    /**
     * E.g. createdDate > 1000 is "createdDate", "gt", 1000
     *
     * @param name     name of field to filter
     * @param operator operator in english form, e.g. gte, lt
     * @param value    value to compare again
     * @return filter range json
     */
    public static JsonNode filterRange(String name, String operator, long value) {
        ObjectNode filter = JsonUtils.createObjectNode();
        filter.putObject("range")
                .putObject(name)
                .put(operator, value);
        return filter;
    }

    /**
     * E.g. createdDate > 1000 is "createdDate", "gt", 1000
     *
     * @param name     name of field to filter
     * @param operator operator in english form, e.g. gte, lt
     * @param value    value to compare again
     * @return filter range json
     */
    public static JsonNode filterRange(String name, String operator, double value) {
        ObjectNode filter = JsonUtils.createObjectNode();
        filter.putObject("range")
                .putObject(name)
                .put(operator, value);
        return filter;
    }

    public static JsonNode sortDistance(String latLng) {
        Objects.requireNonNull(latLng);

        ObjectNode geoDistance = JsonUtils.createObjectNode()
                .put("location.latLng", latLng)
                .put("order", "asc")
                .put("unit", "m")
                .put("mode", "min")
                .put("distance_type", "plane");

        ObjectNode sort = JsonUtils.createObjectNode();
        sort.set("_geo_distance", geoDistance);
        return sort;
    }

    /**
     * @param field field
     * @param by    direction
     * @return { "field": "by" }
     */
    public static JsonNode sortField(String field, String by) {
        ObjectNode sort = JsonUtils.createObjectNode();
        sort.put(field, by);
        return sort;
    }
}
