package munch.data.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import munch.data.brand.Brand;
import munch.data.location.Area;
import munch.data.location.Landmark;
import munch.data.place.Place;
import munch.data.tag.Tag;
import munch.restful.core.JsonUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
            case "Brand":
                return (T) JsonUtils.toObject(source, Brand.class);
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

    public static JsonNode multiMatch(String query, String field, String... fields) {
        ObjectNode root = JsonUtils.createObjectNode();
        ObjectNode match = root.putObject("multi_match");

        match.put("query", query);
        match.put("type", "phrase_prefix");

        ArrayNode fieldsNode = match.putArray("fields");
        fieldsNode.add(field);
        for (String each : fields) fieldsNode.add(each);

        return root;
    }

    /**
     * @param name  to match
     * @param value to match
     * @return JsonNode match filter
     */
    public static JsonNode match(String name, String value) {
        ObjectNode root = JsonUtils.createObjectNode();
        root.putObject("match").put(name, value);
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
    public static JsonNode filterIntersectsPoint(String name, String latLng) {
        ObjectNode filter = JsonUtils.createObjectNode();
        String[] split = latLng.split(",");

        filter.putObject("geo_shape")
                .putObject(name)
                .put("relation", "intersects")
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
            terms.add(text);
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

    /**
     * @param must   function base score
     * @param latLng optional distance decaying with default decay 2.5km scale
     * @return function_score in JsonNode
     */
    public static JsonNode withFunctionScoreMust(JsonNode must, @Nullable String latLng) {
        return withFunctionScoreMust(must, latLng, "2.5km");
    }

    /**
     * @param must   function base score
     * @param latLng latLng for distance decaying
     * @param scale  scale of decay, depending on use cases
     * @return function_score in JsonNode
     */
    public static JsonNode withFunctionScoreMust(JsonNode must, @Nullable String latLng, String scale) {
        ObjectNode root = JsonUtils.createObjectNode();
        ObjectNode function = root.putObject("function_score");
        function.put("score_mode", "multiply");
        function.set("query", must);

        ArrayNode functions = function.putArray("functions");
        functions.addObject()
                .putObject("gauss")
                .putObject("taste.importance")
                .put("scale", "0.1")
                .put("origin", "1");

        if (latLng != null) {
            functions.addObject()
                    .putObject("gauss")
                    .putObject("location.latLng")
                    .put("scale", scale)
                    .put("origin", latLng);
        }
        return root;
    }

    public static final class Suggest {
        public static JsonNode makeCompletion(DataType dataType, @Nullable String latLng, int size) {
            ObjectNode completion = JsonUtils.createObjectNode();
            completion.put("field", "suggest");
            completion.put("fuzzy", true);
            completion.put("size", size);

            ObjectNode contexts = completion.putObject("contexts");
            contexts.set("dataType", makeDataType(dataType));

            if (latLng != null) {
                contexts.set("latLng", makeLatLng(latLng));
            }

            return completion;
        }

        private static JsonNode makeDataType(DataType dataType) {
            ArrayNode arrayNode = JsonUtils.createArrayNode();
            arrayNode.add(dataType.name());
            return arrayNode;
        }

        private static JsonNode makeLatLng(String latLng) {
            String[] lls = latLng.split(",");
            final double lat = Double.parseDouble(lls[0].trim());
            final double lng = Double.parseDouble(lls[1].trim());

            ArrayNode latLngArray = JsonUtils.createArrayNode();

            // +/- 78km
            latLngArray.addObject()
                    .put("precision", 3)
                    .putObject("context")
                    .put("lat", lat)
                    .put("lon", lng);

            // +/- 2.4km
            latLngArray.addObject()
                    .put("precision", 5)
                    .put("boost", 2.0)
                    .putObject("context")
                    .put("lat", lat)
                    .put("lon", lng);
            return latLngArray;
        }
    }

    public static final class Sort {
        /**
         * https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-sort.html
         *
         * @param latLng center
         * @return { "location.latLng" : "lat,lng", "order" : "asc", "unit" : "m", "mode" : "min", "distance_type" : "plane" }
         */
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

    /**
     * Geohash Precision
     * 1:  5,009.4km x 4,992.6km
     * 2:  1,252.3km x 624.1km
     * 3:  156.5km x 156km
     * 4:  39.1km x 19.5km
     * 5:  4.9km x 4.9km
     * 6:  1.2km x 609.4m
     * 7:  152.9m x 152.4m
     * 8:  38.2m x 19m
     * 9:  4.8m x 4.8m
     * 10: 1.2m x 59.5cm
     * 11: 14.9cm x 14.9cm
     * 12: 3.7cm x 1.9cm
     */
    public static final class Spatial {
        public static String[] getBoundingBox(double lat, double lng, double latOffsetKm, double lngOffsetKm) {
            final double latOffset = toRad(latOffsetKm);
            final double lngOffset = toRad(lngOffsetKm);
            return new String[]{
                    (lat + latOffset) + "," + (lng - lngOffset), // Top Lat, Lng
                    (lat - latOffset) + "," + (lng + lngOffset), // Bot Lat, Lng
            };
        }

        public static <T> double[] getCentroid(List<T> list, Function<T, String> mapper) {
            List<String> points = list.stream()
                    .map(mapper)
                    .collect(Collectors.toList());
            return getCentroid(points);
        }

        /**
         * @return centroid of points
         */
        public static double[] getCentroid(List<String> points) {
            double centroidLat = 0, centroidLng = 0;

            for (String point : points) {
                double[] latLng = parse(point);
                centroidLat += latLng[0];
                centroidLng += latLng[1];
            }

            return new double[]{
                    centroidLat / points.size(),
                    centroidLng / points.size()
            };
        }

        public static double toRad(double radiusInKm) {
            return (1 / 110.54) * radiusInKm;
        }

        public static double[] parse(String latLng) {
            Objects.requireNonNull(latLng);

            try {
                String[] split = latLng.split(",");
                double lat = Double.parseDouble(split[0].trim());
                double lng = Double.parseDouble(split[1].trim());
                return new double[]{lat, lng};
            } catch (NullPointerException | IndexOutOfBoundsException | NumberFormatException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
