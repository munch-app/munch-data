package munch.collections;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 9/2/18
 * Time: 1:11 PM
 * Project: munch-core
 */
@Singleton
public final class RecentPlaceClient {
    private static final String DYNAMO_TABLE_NAME = "munch-core.RecentPlace";

    private final Table table;
    private final Index sortIndex;

    @Inject
    public RecentPlaceClient(DynamoDB dynamoDB) {
        this.table = dynamoDB.getTable(DYNAMO_TABLE_NAME);
        this.sortIndex = table.getIndex("sortKey-index");
    }

    public void add(String userId, String placeId) {
        Objects.requireNonNull(userId);
        CollectionClient.validateUUID(placeId, "placeId");

        Item item = new Item();
        item.with("u", userId);
        item.with("p", placeId);
        item.with("c", System.currentTimeMillis());

        table.putItem(item);

    }

    public List<RecentPlace> list(String userId, @Nullable Long maxCreatedDate, int size) {
        Objects.requireNonNull(userId);

        QuerySpec query = new QuerySpec()
                .withHashKey("u", userId)
                .withScanIndexForward(false)
                .withMaxResultSize(size);

        // Set min, max place sort
        if (maxCreatedDate != null) {
            query.withRangeKeyCondition(new RangeKeyCondition("c").lt(maxCreatedDate));
        }

        ItemCollection<QueryOutcome> collection = sortIndex.query(query);

        // Collect results
        List<RecentPlace> addedPlaces = new ArrayList<>();
        collection.forEach(item -> {
            RecentPlace recentPlace = new RecentPlace();
            recentPlace.setPlaceId(item.getString("p"));
            recentPlace.setCreatedDate(item.getLong("c"));
            addedPlaces.add(recentPlace);
        });
        return addedPlaces;
    }
}
