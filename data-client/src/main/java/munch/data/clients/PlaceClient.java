package munch.data.clients;

import com.fasterxml.jackson.databind.JsonNode;
import munch.data.elastic.ElasticClient;
import munch.data.elastic.ElasticIndex;
import munch.data.elastic.ElasticMarshaller;
import munch.data.elastic.query.BoolQuery;
import munch.data.elastic.query.HourFilter;
import munch.data.elastic.query.SortQuery;
import munch.data.structure.Place;
import munch.data.structure.SearchQuery;
import munch.restful.core.exception.ValidationException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 10/10/2017
 * Time: 1:57 AM
 * Project: munch-data
 */
@Singleton
public class PlaceClient {
    private final ElasticIndex elasticIndex;
    private final SearchClient searchClient;

    @Inject
    public PlaceClient(ElasticIndex elasticIndex, SearchClient searchClient) {
        this.elasticIndex = elasticIndex;
        this.searchClient = searchClient;
    }

    /**
     * @param query query object
     * @return List of Place result
     * @see SearchQuery
     */
    public List<Place> search(SearchQuery query) throws IOException {
        return searchClient.search(query);
    }

    // TODO DynamoDB Get/Put/Delete
    public Place get(String id) {
        return null;
    }


    public void put(Place place) throws IOException {
        elasticIndex.put(place);
    }

    public void delete(String id) throws IOException {
        elasticIndex.delete("Place", id);
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

        private List<Place> search(SearchQuery query) throws IOException {
            validate(query);

            // Filter hours
            JsonNode boolNode = this.boolQuery.make(query);
            JsonNode sortNode = this.sortQuery.make(query);
            JsonNode result = client.postBoolSearch("place", query.getFrom(), query.getSize(), boolNode, sortNode);
            JsonNode hits = result.path("hits");

            List<Place> places = marshaller.deserializeList(hits.path("hits"));
            // Filter Hours after query
            HourFilter.filter(query, places);
            return places;
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
