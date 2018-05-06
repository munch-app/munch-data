package munch.collections;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.BatchGetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 9/1/18
 * Time: 3:38 PM
 * Project: munch-core
 */
@Singleton
public final class LikedPlaceClient {
    private static final String DYNAMO_TABLE_NAME = "munch-core.LikedPlace";

    private final DynamoDB dynamoDB;
    private final Table table;
    private final Index sortIndex;

    @Inject
    public LikedPlaceClient(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
        this.table = dynamoDB.getTable(DYNAMO_TABLE_NAME);
        this.sortIndex = table.getIndex("sortKey-index");
    }

    public Set<String> getIsLiked(String userId, Collection<String> placeIds) {
        Objects.requireNonNull(userId);

        PrimaryKey[] primaryKeys = placeIds.stream()
                .map(s -> new PrimaryKey("u", userId, "p", s))
                .toArray(PrimaryKey[]::new);

        BatchGetItemSpec spec = new BatchGetItemSpec()
                .withTableKeyAndAttributes(new TableKeysAndAttributes(DYNAMO_TABLE_NAME)
                        .withPrimaryKeys(primaryKeys));

        BatchGetItemOutcome outcome = dynamoDB.batchGetItem(spec);
        return outcome.getTableItems()
                .get(DYNAMO_TABLE_NAME)
                .stream()
                .filter(Objects::nonNull)
                .map(item -> item.getString("p"))
                .collect(Collectors.toSet());
    }

    /**
     * @param userId  user id of person to check
     * @param placeId id of place to check
     * @return whether the user liked to place
     */
    public boolean isLiked(String userId, String placeId) {
        Objects.requireNonNull(userId);
        CollectionClient.validateUUID(placeId, "placeId");

        GetItemSpec getSpec = new GetItemSpec();
        getSpec.withPrimaryKey("u", userId, "p", placeId);
        getSpec.withAttributesToGet("u", "p");
        return table.getItem(getSpec) != null;
    }

    /**
     * Add place to liked
     *
     * @param userId  user id of person
     * @param placeId id of place
     */
    public void add(String userId, String placeId) {
        Objects.requireNonNull(userId);
        CollectionClient.validateUUID(placeId, "placeId");

        Item item = new Item();
        item.with("u", userId);
        item.with("p", placeId);
        item.with("s", System.currentTimeMillis());
        item.with("c", System.currentTimeMillis());
        table.putItem(item);
    }

    /**
     * Remove place from liked
     *
     * @param userId  user id of person
     * @param placeId id of place
     */
    public void remove(String userId, String placeId) {
        Objects.requireNonNull(userId);
        CollectionClient.validateUUID(placeId, "placeId");

        table.deleteItem("u", userId, "p", placeId);
    }

    /**
     * @param userId     user id of person
     * @param maxSortKey max id of place, don't show result after this id
     * @param size       size per query
     * @return List of Place liked by the user
     */
    public List<LikedPlace> list(String userId, @Nullable Long maxSortKey, int size) {
        Objects.requireNonNull(userId);

        QuerySpec query = new QuerySpec()
                .withHashKey("u", userId)
                .withScanIndexForward(false)
                .withMaxResultSize(size);


        // Set min, max place sort
        if (maxSortKey != null) {
            query.withRangeKeyCondition(new RangeKeyCondition("s").lt(maxSortKey));
        }

        ItemCollection<QueryOutcome> collection = sortIndex.query(query);

        // Collect results
        List<LikedPlace> addedPlaces = new ArrayList<>();
        collection.forEach(item -> {
            LikedPlace addedPlace = new LikedPlace();
            addedPlace.setPlaceId(item.getString("p"));
            addedPlace.setSortKey(item.getLong("s"));
            addedPlace.setCreatedDate(item.getLong("c"));
            addedPlaces.add(addedPlace);
        });
        return addedPlaces;
    }
}
