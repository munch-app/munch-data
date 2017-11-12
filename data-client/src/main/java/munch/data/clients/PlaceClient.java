package munch.data.clients;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.fasterxml.jackson.databind.JsonNode;
import munch.data.elastic.ElasticClient;
import munch.data.elastic.ElasticIndex;
import munch.data.elastic.ElasticMarshaller;
import munch.data.elastic.query.BoolQuery;
import munch.data.elastic.query.HourFilter;
import munch.data.elastic.query.SortQuery;
import munch.data.structure.Place;
import munch.data.structure.SearchQuery;
import munch.restful.core.exception.JsonException;
import munch.restful.core.exception.ValidationException;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 10/10/2017
 * Time: 1:57 AM
 * Project: munch-data
 */
@Singleton
public class PlaceClient extends AbstractClient {
    public static final String DYNAMO_TABLE_NAME = "munch-data.Place";

    private final ElasticIndex elasticIndex;
    private final SearchClient searchClient;
    private final Table placeTable;

    @Inject
    public PlaceClient(ElasticIndex elasticIndex, SearchClient searchClient, DynamoDB dynamoDB) {
        this.elasticIndex = elasticIndex;
        this.searchClient = searchClient;
        this.placeTable = dynamoDB.getTable(DYNAMO_TABLE_NAME);
    }

    /**
     * @param query query object
     * @return List of Place result
     * @see SearchQuery
     */
    public List<Place> search(SearchQuery query) {
        return searchClient.search(query);
    }

    /**
     * @param query query
     * @return total possible results count
     */
    public long count(SearchQuery query) {
        return searchClient.count(query);
    }

    /**
     * @param id id of place
     * @return Place from dynamo
     * @throws JsonException parsing error
     */
    @Nullable
    public Place get(String id) throws JsonException {
        Objects.requireNonNull(id);

        Item item = placeTable.getItem("_id", id);
        if (item == null) return null;

        String json = item.getJSON("_source");
        return fromJson(json, Place.class);
    }

    /**
     * Put place to dynamo first then elastic
     *
     * @param place put place to dynamo and elastic
     * @throws JsonException parsing error
     */
    public void put(Place place) throws JsonException {
        Objects.requireNonNull(place.getId());
        Objects.requireNonNull(place.getName());

        Item item = new Item()
                .withString("_id", place.getId())
                .withJSON("_source", toJson(place));
        placeTable.putItem(item);
        elasticIndex.put(place);
    }

    /**
     * Delete place from elastic first then dynamo
     *
     * @param id id of place to delete
     */
    public void delete(String id) {
        Objects.requireNonNull(id);

        elasticIndex.delete("Place", id);
        placeTable.deleteItem("_id", id);
    }

    @Singleton
    private static final class SearchClient {
        private final ElasticClient client;
        private final BoolQuery boolQuery;
        private final SortQuery sortQuery;
        private final ElasticMarshaller marshaller;

        @Inject
        private SearchClient(ElasticClient client, BoolQuery boolQuery, SortQuery sortQuery, ElasticMarshaller marshaller) {
            this.client = client;
            this.boolQuery = boolQuery;
            this.sortQuery = sortQuery;
            this.marshaller = marshaller;
        }

        private List<Place> search(SearchQuery query) {
            validate(query);

            JsonNode boolNode = this.boolQuery.make(query);
            JsonNode sortNode = this.sortQuery.make(query);
            JsonNode result = client.postBoolSearch("Place", query.getFrom(), query.getSize(), boolNode, sortNode);
            JsonNode hits = result.path("hits");

            List<Place> places = marshaller.deserializeList(hits.path("hits"));
            // Filter Hours after query
            HourFilter.filter(query, places);
            return places;
        }

        /**
         * @param query query
         * @return total possible results
         */
        private long count(SearchQuery query) {
            validate(query);

            JsonNode boolNode = this.boolQuery.make(query);
            return client.postBoolCount("Place", boolNode);
        }

        /**
         * Validate from, size
         * Validate points must be more than 3
         *
         * @param query query to validate and fix
         */
        @SuppressWarnings("ResultOfMethodCallIgnored")
        private void validate(SearchQuery query) {
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
}
