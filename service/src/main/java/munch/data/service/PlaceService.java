package munch.data.service;

import com.amazonaws.services.dynamodbv2.document.BatchGetItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes;
import com.amazonaws.services.dynamodbv2.document.spec.BatchGetItemSpec;
import com.fasterxml.jackson.databind.JsonNode;
import munch.data.elastic.ElasticIndex;
import munch.data.place.Place;
import munch.restful.core.JsonUtils;
import munch.restful.core.KeyUtils;
import munch.restful.server.JsonCall;
import munch.restful.server.dynamodb.RestfulDynamoHashService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 2/6/18
 * Time: 4:24 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceService extends RestfulDynamoHashService<Place> {
    private static final String TABLE_NAME = "munch-data.Place";
    private final DynamoDB dynamoDB;
    private final ElasticIndex elasticIndex;

    @Inject
    public PlaceService(DynamoDB dynamoDB, ElasticIndex elasticIndex) {
        super(dynamoDB.getTable(TABLE_NAME), Place.class, "placeId", 100);
        this.dynamoDB = dynamoDB;
        this.elasticIndex = elasticIndex;
    }

    @Override
    public void route() {
        PATH("/places", () -> {
            GET("", this::list);
            GET("/:placeId", this::get);
            POST("/batch/get", this::batchGet);

            POST("", this::post);
            PUT("/:placeId", this::put);
            DELETE("/:placeId", this::delete);
        });
    }

    private JsonNode post(JsonCall call) {
        Place place = call.bodyAsObject(Place.class);
        place.setPlaceId(KeyUtils.randomUUIDBase64());
        place.setCreatedMillis(System.currentTimeMillis());
        return put(place);
    }

    public JsonNode put(JsonCall call) {
        Place place = call.bodyAsObject(Place.class);
        place.setPlaceId(call.pathString("placeId"));
        return put(place);
    }

    public Place delete(JsonCall call) {
        String placeId = call.pathString("placeId");
        elasticIndex.delete("Place", placeId);
        return super.delete(placeId);
    }

    private JsonNode put(Place place) {
        place.setUpdatedMillis(System.currentTimeMillis());

        elasticIndex.put(place);
        super.put(place.getPlaceId(), JsonUtils.toTree(place));
        return nodes(200, place);
    }

    public Map<String, Place> batchGet(JsonCall call) {
        List<String> placeIds = call.bodyAsList(String.class);
        if (placeIds.isEmpty()) return Map.of();

        BatchGetItemSpec spec = new BatchGetItemSpec()
                .withTableKeyAndAttributes(new TableKeysAndAttributes(TABLE_NAME)
                        .withHashOnlyKeys("placeId", placeIds.toArray(new Object[0])));

        BatchGetItemOutcome outcome = dynamoDB.batchGetItem(spec);

        Map<String, Place> placeMap = outcome.getTableItems()
                .get(TABLE_NAME)
                .stream()
                .filter(Objects::nonNull)
                .map(s -> JsonUtils.toObject(s.toJSON(), Place.class))
                .collect(Collectors.toMap(Place::getPlaceId, o -> o));

        for (String placeId : placeIds) {
            placeMap.putIfAbsent(placeId, null);
        }
        return placeMap;
    }
}
