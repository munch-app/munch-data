package munch.collections;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.Select;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static munch.collections.CollectionClient.validateUUID;

/**
 * Created by: Fuxing
 * Date: 16/1/2018
 * Time: 10:29 AM
 * Project: munch-core
 */
@Singleton
public final class CollectionPlaceClient {
    private static final String DYNAMO_TABLE_NAME = "munch-core.PlaceCollection.AddedPlace";

    private final Table table;
    private final Index sortIndex;

    @Inject
    public CollectionPlaceClient(DynamoDB dynamoDB) {
        this.table = dynamoDB.getTable(DYNAMO_TABLE_NAME);
        this.sortIndex = table.getIndex("sortKey-index");
    }

    public void add(String userId, String collectionId, String placeId) {
        Objects.requireNonNull(userId);
        validateUUID(placeId, "placeId");

        Item item = new Item();
        item.with("uc", createKey(userId, collectionId));
        item.with("p", placeId);
        item.with("s", System.currentTimeMillis());
        item.with("c", System.currentTimeMillis());
        table.putItem(item);
    }

    public void remove(String userId, String collectionId, String placeId) {
        Objects.requireNonNull(userId);
        validateUUID(placeId, "placeId");

        table.deleteItem("uc", createKey(userId, collectionId), "p", placeId);
    }

    public long count(String userId, String collectionId) {
        Objects.requireNonNull(userId);

        QuerySpec query = new QuerySpec()
                .withHashKey("uc", createKey(userId, collectionId))
                .withSelect(Select.COUNT);
        ItemCollection<QueryOutcome> outcome = table.query(query);
        for (Item item : outcome) {
        }
        return outcome.getAccumulatedItemCount();
    }

    public List<PlaceCollection.AddedPlace> list(String userId, String collectionId, @Nullable Long maxSortKey, int size) {
        Objects.requireNonNull(userId);

        QuerySpec query = new QuerySpec()
                .withHashKey("uc", createKey(userId, collectionId))
                .withScanIndexForward(false)
                .withMaxResultSize(size);


        // Set min, max place sort
        if (maxSortKey != null) {
            query.withRangeKeyCondition(new RangeKeyCondition("s").lt(maxSortKey));
        }

        ItemCollection<QueryOutcome> collection = sortIndex.query(query);

        // Collect results
        List<PlaceCollection.AddedPlace> addedPlaces = new ArrayList<>();
        collection.forEach(item -> addedPlaces.add(fromItem(item)));
        return addedPlaces;
    }

    private static String createKey(String userId, String collectionId) {
        validateUUID(collectionId, "collectionId");
        return userId + "_" + collectionId;
    }

    private static PlaceCollection.AddedPlace fromItem(Item item) {
        PlaceCollection.AddedPlace addedPlace = new PlaceCollection.AddedPlace();
        addedPlace.setPlaceId(item.getString("p"));
        addedPlace.setSortKey(item.getLong("s"));
        addedPlace.setCreatedDate(new Date(item.getLong("c")));
        return addedPlace;
    }
}
