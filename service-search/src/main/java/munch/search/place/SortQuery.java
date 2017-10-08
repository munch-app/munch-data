package munch.search.place;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import munch.data.SearchQuery;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 21/7/2017
 * Time: 5:56 PM
 * Project: munch-core
 */
@Singleton
public final class SortQuery {
    private static final Logger logger = LoggerFactory.getLogger(SortQuery.class);
    private final ObjectMapper mapper;

    @Inject
    public SortQuery(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * @param query SearchQuery for place
     * @return created bool node
     */
    public JsonNode make(SearchQuery query) {
        ArrayNode sortArray = mapper.createArrayNode();

        // If it is null or blank, return default munchRank
        if (query.getSort() == null || StringUtils.isBlank(query.getSort().getType())) {
            sortArray.add(sortField("munchRank", "desc"));
            return sortArray;
        }

        switch (query.getSort().getType().toLowerCase()) {
            case SearchQuery.Sort.TYPE_PRICE_LOWEST:
                sortArray.add(sortField("price.middle", "asc"));
                break;
            case SearchQuery.Sort.TYPE_PRICE_HIGHEST:
                sortArray.add(sortField("price.middle", "desc"));
                break;
            case SearchQuery.Sort.TYPE_DISTANCE_NEAREST:
                if (StringUtils.isNotBlank(query.getLatLng())) {
                    logger.warn("Sort by distance by latLng not provided in query.latLng");
                    sortArray.add(sortDistance(query.getLatLng()));
                }
                break;
            case SearchQuery.Sort.TYPE_RATING_HIGHEST:
                // TODO Type Rating in Future
                break;

            default:
            case SearchQuery.Sort.TYPE_MUNCH_RANK:
                sortArray.add(sortField("munchRank", "desc"));
                break;
        }

        return sortArray;
    }

    /**
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-sort.html
     *
     * @param latLng center
     * @return { "location.latLng" : "lat,lng", "order" : "asc", "unit" : "m", "mode" : "min", "distance_type" : "plane" }
     */
    private JsonNode sortDistance(String latLng) {
        Objects.requireNonNull(latLng);

        ObjectNode geoDistance = mapper.createObjectNode()
                .put("location.latLng", latLng)
                .put("order", "asc")
                .put("unit", "m")
                .put("mode", "min")
                .put("distance_type", "plane");

        ObjectNode sort = mapper.createObjectNode();
        sort.set("_geo_distance", geoDistance);
        return sort;
    }

    /**
     * @param field field
     * @param by    direction
     * @return { "field": "by" }
     */
    private JsonNode sortField(String field, String by) {
        ObjectNode sort = mapper.createObjectNode();
        sort.put(field, by);
        return sort;
    }
}
