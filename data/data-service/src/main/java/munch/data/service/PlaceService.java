package munch.data.service;

import com.amazonaws.services.dynamodbv2.document.BatchGetItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes;
import com.amazonaws.services.dynamodbv2.document.spec.BatchGetItemSpec;
import munch.data.elastic.ElasticIndex;
import munch.data.place.Place;
import munch.restful.core.JsonUtils;
import munch.restful.server.JsonCall;

import javax.annotation.Nullable;
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
public final class PlaceService extends PersistenceService<Place> {

    private final DynamoDB dynamoDB;
    private final ClusterManager clusterManager;

    @Inject
    public PlaceService(PersistenceMapping persistenceMapping, ElasticIndex elasticIndex, DynamoDB dynamoDB, ClusterManager clusterManager) {
        super(persistenceMapping, elasticIndex, Place.class);
        this.dynamoDB = dynamoDB;
        this.clusterManager = clusterManager;
    }


    @Override
    public void route() {
        PATH("/places", () -> {
            GET("", this::list);
            GET("/:placeId", this::get);
            POST("/batch/get", this::batchGet);

            PUT("/:placeId", this::put);
            DELETE("/:placeId", this::delete);
        });
    }

    @Override
    public Place put(Place object) {
        clusterManager.update(object);
        return super.put(object);
    }

    public Map<String, Place> batchGet(JsonCall call) {
        List<String> placeIds = call.bodyAsList(String.class);
        if (placeIds.isEmpty()) return Map.of();

        BatchGetItemSpec spec = new BatchGetItemSpec()
                .withTableKeyAndAttributes(new TableKeysAndAttributes(table.getTableName())
                        .withHashOnlyKeys("placeId", placeIds.toArray(new Object[0])));

        BatchGetItemOutcome outcome = dynamoDB.batchGetItem(spec);

        Map<String, Place> placeMap = outcome.getTableItems()
                .get(table.getTableName())
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
