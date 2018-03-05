package munch.awards;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by: Fuxing
 * Date: 4/3/2018
 * Time: 9:27 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceAwardClient {
    private static final String DYNAMO_TABLE_NAME = "munch-data.PlaceAward";

    private final Table table;

    @Inject
    public PlaceAwardClient(DynamoDB dynamoDB) {
        this.table = dynamoDB.getTable(DYNAMO_TABLE_NAME);
    }

    public List<PlaceAward> list(String placeId, @Nullable String maxCollectionAwardId, int size) {
        Objects.requireNonNull(placeId);

        QuerySpec query = new QuerySpec()
                .withHashKey("p", placeId)
                .withScanIndexForward(false)
                .withMaxResultSize(size);


        // Set min, max place sort
        if (maxCollectionAwardId != null) {
            query.withRangeKeyCondition(new RangeKeyCondition("ca").lt(maxCollectionAwardId));
        }

        ItemCollection<QueryOutcome> collection = table.query(query);

        // Collect results
        List<PlaceAward> addedPlaces = new ArrayList<>();
        collection.forEach(item -> addedPlaces.add(fromItem(item)));
        return addedPlaces;
    }

    public void put(String placeId, long collectionId, long awardId, String awardName, String userId) {
        Item item = new Item();
        item.with("p", placeId);
        item.with("ca", createCollectionAwardId(collectionId, awardId));
        item.with("n", awardName);
        item.with("u", userId);
        table.putItem(item);
    }

    public void delete(String placeId, long collectionId, long awardId) {
        table.deleteItem("p", placeId, "ca", createCollectionAwardId(collectionId, awardId));
    }

    /**
     * Composite of collectionId and awardId to form UUID
     *
     * @param collectionId collection id
     * @param awardId      award id
     * @return UUID
     */
    private static String createCollectionAwardId(long collectionId, long awardId) {
        UUID uuid = new UUID(collectionId, awardId);
        return uuid.toString();
    }

    private static PlaceAward fromItem(Item item) {
        String collectionAwardId = item.getString("ca");
        long collectionId = UUID.fromString(collectionAwardId).getMostSignificantBits();
        String collectionIdForCollectionClient = new UUID(collectionId, 0).toString();
        String awardName = item.getString("n");
        String userId = item.getString("u");

        PlaceAward placeAward = new PlaceAward();
        placeAward.setAwardName(awardName);
        placeAward.setCollectionAwardId(collectionAwardId);
        placeAward.setUserId(userId);
        placeAward.setCollectionId(collectionIdForCollectionClient);
        return placeAward;
    }
}
