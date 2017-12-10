package munch.data.elastic.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import munch.data.structure.Location;
import munch.data.structure.SearchQuery;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created By: Fuxing Loh
 * Date: 22/3/2017
 * Time: 9:22 PM
 * Project: munch-core
 */
@Singleton
public final class BoolQuery {
    private final ObjectMapper mapper;

    @Inject
    public BoolQuery(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * @param query SearchQuery for place
     * @return created bool node
     */
    public JsonNode make(SearchQuery query) {
        ObjectNode bool = mapper.createObjectNode();
        bool.set("must", must(query.getQuery()));
        bool.set("must_not", mustNot(query.getFilter()));
        bool.set("filter", filter(query));
        return bool;
    }

    /**
     * Search with text on name
     *
     * @param query query string
     * @return JsonNode must filter
     */
    private JsonNode must(String query) {
        ObjectNode root = mapper.createObjectNode();

        // Match all if query is blank
        if (StringUtils.isBlank(query)) {
            root.putObject("match_all");
            return root;
        }

        // Match name if got query
        ObjectNode multiMatch = root.putObject("multi_match");
        multiMatch.put("query", query);
        multiMatch.put("fuzziness", 1);
        multiMatch.putArray("fields")
                .add("name^5")
                .add("tag.explicits^2")
                .add("tag.implicits");
        return root;
    }

    /**
     * Filter to must not
     *
     * @param filter filters
     * @return JsonNode must_not filter
     */
    private JsonNode mustNot(SearchQuery.Filter filter) {
        ArrayNode notArray = mapper.createArrayNode();
        if (filter == null) return notArray;
        if (filter.getTag() == null) return notArray;
        if (filter.getTag().getNegatives() == null) return notArray;

        // Must not filters
        for (String tag : filter.getTag().getNegatives()) {
            notArray.add(filterTerm("tag.explicits", tag.toLowerCase()));
        }
        return notArray;
    }

    /**
     * @param searchQuery search query
     * @return JsonNode bool filter
     */
    private JsonNode filter(SearchQuery searchQuery) {
        ArrayNode filterArray = mapper.createArrayNode();

        // Polygon if location exists
        Location location = searchQuery.getLocation();
        if (location != null && location.getPoints() != null) {
            filterArray.add(filterPolygon(location.getPoints()));
        } else if (searchQuery.getLatLng() != null) {
            filterArray.add(filterDistance(searchQuery.getLatLng(), 1000));
        }

        // Check if filter is not null before continuing
        SearchQuery.Filter filter = searchQuery.getFilter();
        if (filter == null) return filterArray;

        // Filter to positive tags
        if (filter.getTag() != null && filter.getTag().getPositives() != null) {
            for (String tag : filter.getTag().getPositives()) {
                filterArray.add(filterTerm("tag.explicits", tag.toLowerCase()));
            }
        }

        // Filter price
        if (filter.getPrice() != null) {
            ObjectNode range = mapper.createObjectNode();
            if (filter.getPrice().getMax() != null) {
                range.put("lte", filter.getPrice().getMax());
            }

            if (filter.getPrice().getMin() != null) {
                range.put("gte", filter.getPrice().getMin());
            }

            // Only add if contains max or min
            if (range.size() > 0) {
                // Filter is applied on middle
                filterArray.add(mapper.createObjectNode().set("price.middle", range));
            }
        }

        // Filter hours is done at client side
        return filterArray;
    }

    /**
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-geo-polygon-query.html
     *
     * @param pointList list of points to form a polygon
     * @return JsonNode = { "geo_polygon": { "location.latLng": { "points": ["-1,2", "-5,33" ...]}}}
     */
    private JsonNode filterPolygon(List<String> pointList) {
        ObjectNode filter = mapper.createObjectNode();
        ArrayNode points = filter.putObject("geo_polygon")
                .putObject("location.latLng")
                .putArray("points");
        pointList.forEach(points::add);
        return filter;
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
                .put("location.latLng", latLng);
        return filter;
    }

    /**
     * @param name name of term
     * @param text text of term
     * @return JsonNode =  { "term" : { "name" : "text" } }
     */
    private JsonNode filterTerm(String name, String text) {
        ObjectNode filter = mapper.createObjectNode();
        filter.putObject("term").put(name, text);
        return filter;
    }
}
