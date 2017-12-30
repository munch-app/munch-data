package munch.data.elastic.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Singleton;
import munch.data.structure.Container;
import munch.data.structure.Location;
import munch.data.structure.SearchQuery;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Created By: Fuxing Loh
 * Date: 22/3/2017
 * Time: 9:22 PM
 * Project: munch-core
 */
@Singleton
public final class PlaceBoolQuery {
    private static final Logger logger = LoggerFactory.getLogger(PlaceBoolQuery.class);
    private static final ObjectMapper mapper = JsonUtils.objectMapper;

    /**
     * NOTE: This BoolQuery is only for Place data type
     *
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
        filterArray.add(filterTerm("dataType", "Place"));

        // Filter 'Container' else 'Location' else 'LatLng' else none
        filterLocation(searchQuery).ifPresent(filterArray::add);

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
                ObjectNode rangeFilter = mapper.createObjectNode();
                rangeFilter.putObject("range").set("price.middle", range);
                filterArray.add(rangeFilter);
            }
        }

        // Filter hours is done at client side
        return filterArray;
    }

    /**
     * @param searchQuery searchQuery
     * @return Filter Location Json
     */
    private Optional<JsonNode> filterLocation(SearchQuery searchQuery) {
        SearchQuery.Filter filter = searchQuery.getFilter();

        JsonNode locationFilter = filterContainer(filter);
        if (locationFilter != null) return Optional.of(locationFilter);

        locationFilter = filterPolygon(filter);
        if (locationFilter != null) return Optional.of(locationFilter);

        if (searchQuery.getLatLng() != null) {
            double radius = searchQuery.getRadius() != null ? searchQuery.getRadius() : 1000;
            JsonNode filtered = filterDistance(searchQuery.getLatLng(), radius);
            return Optional.of(filtered);
        }

        return Optional.empty();
    }

    /**
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-terms-query.html
     *
     * @param filter object containing containers object for filtering
     * @return { "terms" : { "containers.id" : ["id1", "id2"] } }
     */
    @Nullable
    private static JsonNode filterContainer(SearchQuery.Filter filter) {
        if (filter == null) return null;

        List<Container> containers = filter.getContainers();
        if (containers == null) return null;
        if (containers.isEmpty()) return null;

        ObjectNode filterObject = mapper.createObjectNode();
        ArrayNode values = filterObject.putObject("terms").putArray("containers.id");
        for (Container container : containers) {
            // Malformed == return also
            if (StringUtils.isEmpty(container.getId())) {
                logger.warn("SearchQuery.Filter.Containers.id is blank. SearchQuery.Filter: {}", filter);
                return null;
            }
            values.add(container.getId());
        }
        return filterObject;
    }

    private JsonNode filterPolygon(SearchQuery.Filter filter) {
        if (filter == null) return null;

        Location location = filter.getLocation();
        if (location == null) return null;
        if (location.getPoints() == null) return null;
        if (location.getPoints().size() < 3) {
            logger.warn("SearchQuery.Filter.Location.points < 3. SearchQuery.Filter: {}", filter);
            return null;
        }

        return filterPolygon(location.getPoints());
    }

    /**
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-geo-polygon-query.html
     *
     * @param pointList list of points to form a polygon
     * @return JsonNode = { "geo_polygon": { "location.latLng": { "points": ["-1,2", "-5,33" ...]}}}
     */
    private static JsonNode filterPolygon(List<String> pointList) {
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
    private static JsonNode filterDistance(String latLng, double metres) {
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
    private static JsonNode filterTerm(String name, String text) {
        ObjectNode filter = mapper.createObjectNode();
        filter.putObject("term").put(name, text);
        return filter;
    }
}
