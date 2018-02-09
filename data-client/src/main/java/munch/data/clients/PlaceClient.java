package munch.data.clients;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.BatchGetItemSpec;
import com.fasterxml.jackson.databind.JsonNode;
import munch.data.elastic.ElasticClient;
import munch.data.elastic.ElasticIndex;
import munch.data.elastic.ElasticMarshaller;
import munch.data.elastic.query.BoolQuery;
import munch.data.elastic.query.SortQuery;
import munch.data.exceptions.ElasticException;
import munch.data.structure.Place;
import munch.data.structure.SearchQuery;
import munch.restful.core.exception.JsonException;
import munch.restful.core.exception.ValidationException;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 10/10/2017
 * Time: 1:57 AM
 * Project: munch-data
 */
@Singleton
public class PlaceClient extends AbstractClient {
    public static final String DYNAMO_TABLE_NAME = "munch-data2.Place";

    private final ElasticIndex elasticIndex;
    private final SearchClient searchClient;
    private final Table placeTable;

    private final DynamoDB dynamoDB;

    @Inject
    public PlaceClient(ElasticIndex elasticIndex, SearchClient searchClient, DynamoDB dynamoDB) {
        this.elasticIndex = elasticIndex;
        this.searchClient = searchClient;
        this.dynamoDB = dynamoDB;
        this.placeTable = dynamoDB.getTable(DYNAMO_TABLE_NAME);
    }

    /**
     * @return Place Search Client
     */
    public SearchClient getSearchClient() {
        return searchClient;
    }

    /**
     * @param ids list of place id
     * @return list of Place in order, with those not found removed
     */
    public List<Place> batchGet(List<String> ids) {
        BatchGetItemSpec spec = new BatchGetItemSpec()
                .withTableKeyAndAttributes(new TableKeysAndAttributes(DYNAMO_TABLE_NAME)
                        .withHashOnlyKeys("_id", ids.toArray(new Object[ids.size()])));

        BatchGetItemOutcome outcome = dynamoDB.batchGetItem(spec);
        return outcome.getTableItems()
                .get(DYNAMO_TABLE_NAME)
                .stream()
                .filter(Objects::nonNull)
                .map(item -> item.getJSON("_source"))
                .map(s -> fromJson(s, Place.class))
                .collect(Collectors.toList());
    }

    /**
     * @param ids list of place id
     * @return Map of Place, with placeId -> Place mapping
     */
    public Map<String, Place> batchGetMap(List<String> ids) {
        return batchGet(ids)
                .stream()
                .collect(Collectors.toMap(Place::getId, o -> o));
    }

    /**
     * @param dataList  data list to map from
     * @param idMapper  data to place id mapper
     * @param collector data collector from (T, Optional Place) to Optional R
     * @param <R>       Return type
     * @param <T>       Data Type
     * @return List of mapped result
     */
    public <R, T> List<R> batchGetMap(List<T> dataList, Function<T, String> idMapper, BiFunction<T, Place, R> collector) {
        List<String> placeIds = dataList.stream().map(idMapper).collect(Collectors.toList());
        Map<String, Place> placeMap = batchGetMap(placeIds);
        return dataList.stream()
                .map(t -> {
                    String id = idMapper.apply(t);
                    Place place = placeMap.get(id);
                    return collector.apply(t, place);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * @param dataList data list to map from
     * @param idMapper data to place id mapper
     * @param consumer consumer into Anything -> from Data, Place
     * @param <T>      Data Type
     */
    public <T> void batchGetForEach(List<T> dataList, Function<T, String> idMapper, BiConsumer<T, Place> consumer) {
        List<String> placeIds = dataList.stream().map(idMapper).collect(Collectors.toList());
        Map<String, Place> placeMap = batchGetMap(placeIds);
        for (T data : dataList) {
            String id = idMapper.apply(data);
            consumer.accept(data, placeMap.get(id));
        }
    }

    /**
     * @param id id of place
     * @return Place from dynamo
     * @throws JsonException parsing error
     */
    @Nullable
    public Place get(String id) throws ElasticException {
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
    public void put(Place place) throws ElasticException {
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
    public void delete(String id) throws ElasticException {
        Objects.requireNonNull(id);

        elasticIndex.delete("Place", id);
        placeTable.deleteItem("_id", id);
    }

    @Singleton
    public static final class SearchClient {
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

        public List<Place> search(SearchQuery query) {
            validate(query);

            JsonNode boolNode = this.boolQuery.make(query);
            JsonNode sortNode = this.sortQuery.make(query);
            JsonNode result = client.postBoolSearch(query.getFrom(), query.getSize(), boolNode, sortNode);
            JsonNode hits = result.path("hits");

            return marshaller.deserializeList(hits.path("hits"));
        }

        /**
         * @param query query
         * @return total possible results
         */
        public long count(SearchQuery query) {
            validate(query);

            JsonNode boolNode = this.boolQuery.make(query);
            return client.postBoolCount(boolNode);
        }

        /**
         * @param node raw node search
         * @return place result
         */
        public List<Place> search(JsonNode node) {
            JsonNode result = client.postSearch(node);
            JsonNode hits = result.path("hits");
            return marshaller.deserializeList(hits.path("hits"));
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
        }
    }
}
