package munch.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import munch.data.SearchQuery;
import munch.restful.core.exception.ValidationException;
import munch.restful.server.JsonCall;
import munch.restful.server.JsonService;
import munch.search.elastic.ElasticClient;
import munch.search.elastic.ElasticMarshaller;
import munch.search.place.BoolQuery;
import munch.search.place.HourFilter;
import munch.search.place.SortQuery;

import java.io.IOException;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 6/7/2017
 * Time: 6:36 AM
 * Project: munch-core
 */
@Singleton
public class SearchService implements JsonService {

    private final ElasticClient client;
    private final BoolQuery boolQuery;
    private final SortQuery sortQuery;
    private final ElasticMarshaller marshaller;
    private final ObjectMapper mapper;

    @Inject
    public SearchService(ElasticClient client, BoolQuery boolQuery, SortQuery sortQuery, ElasticMarshaller marshaller, ObjectMapper mapper) {
        this.client = client;
        this.boolQuery = boolQuery;
        this.sortQuery = sortQuery;
        this.marshaller = marshaller;
        this.mapper = mapper;
    }

    @Override
    public void route() {
        // Root service can return any results
        POST("/search", this::search);
        POST("/suggest", this::suggest);
    }

    /**
     * Currently search only support place data
     * <p>
     * query: String = is for place name search
     * filter: Filters = apply filter for bounding search
     * from: Int = start from
     * size: Int = size of query
     *
     * @param call json call
     * @return { "data": [places], "total": size}
     */
    private JsonNode search(JsonCall call) throws IOException {
        // Validate and search for error and fixes it
        SearchQuery query = call.bodyAsObject(SearchQuery.class);
        validate(query);

        // Filter hours
        JsonNode boolNode = this.boolQuery.make(query);
        JsonNode sortNode = this.sortQuery.make(query);
        JsonNode result = client.postBoolSearch("place", query.getFrom(), query.getSize(), boolNode, sortNode);
        JsonNode hits = result.path("hits");

        List<Object> places = marshaller.deserializeList(hits.path("hits"));
        // Filter Hours after query
        HourFilter.filter(query, places);

        // Return data: [] with total: Integer & linked: {} object
        ObjectNode nodes = nodes(200, places);
        nodes.put("total", hits.path("total").asInt());
        return nodes;
    }

    /**
     * Note: Geometry follows elastic type hence follow:
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/geo-shape.html
     * Geometry intersect function is applied
     * <pre>
     * {
     *     size: 20,
     *     query: "", // Mandatory
     *     latLng: "lat,lng" // Optional
     * }
     * </pre>
     * <p>
     * query: String = is for place name search
     * latLng: String = to provide radius context to the suggestion
     * size: Int = size of query
     *
     * @param call    json call
     * @param request json body
     * @return { "data": [places, locations] }
     */
    private JsonNode suggest(JsonCall call, JsonNode request) throws IOException {
        int size = ValidationException.require("size", request.path("size")).asInt();
        String query = ValidationException.requireNonBlank("query", request.path("query"));
        String latLng = request.path("latLng").asText(null);
        JsonNode results = client.suggest(null, query, latLng, size);
        return nodes(200, marshaller.deserializeList(results));
    }

    /**
     * Validate from, size
     * Validate points must be more than 3
     *
     * @param query query to validate and fix
     */
    private static void validate(SearchQuery query) {
        // From and Size not null validation
        ValidationException.requireNonNull("from", query.getFrom());
        ValidationException.requireNonNull("size", query.getSize());

        // Check if location contains polygon if exist
        if (query.getLocation() != null && query.getLocation().getPoints() != null) {
            if (query.getLocation().getPoints().size() < 3) {
                throw new ValidationException("location.points", "Points must have at least 3 points.");
            }
        }
    }
}
