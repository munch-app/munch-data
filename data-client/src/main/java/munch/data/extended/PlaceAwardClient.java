package munch.data.extended;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 4/3/2018
 * Time: 9:27 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceAwardClient extends ExtendedDataClient<PlaceAward> {

    @Inject
    public PlaceAwardClient(DynamoDB dynamoDB) {
        super(dynamoDB.getTable("munch-data.PlaceAward"));
    }

    @Override
    protected PlaceAward fromItem(Item item) {
        PlaceAward placeAward = new PlaceAward();
        placeAward.setSortKey(item.getString(sortKeyName));

        placeAward.setUserId(item.getString("userId"));
        placeAward.setCollectionId(item.getString("collectionId"));
        placeAward.setAwardName(item.getString("awardName"));
        return placeAward;
    }

    @Override
    protected Item toItem(String placeId, PlaceAward data) {
        Item item = new Item();
        item.with(hashKeyName, placeId);
        item.with(sortKeyName, data.getSortKey());

        item.with("userId", data.getUserId());
        item.with("collectionId", data.getCollectionId());
        item.with("awardName", data.getAwardName());
        return item;
    }
}
