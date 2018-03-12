package munch.data.extended;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 6/3/2018
 * Time: 12:54 AM
 * Project: munch-data
 */
@Singleton
public final class PlaceMenuClient extends ExtendedDataClient<PlaceMenu> {

    @Inject
    public PlaceMenuClient(DynamoDB dynamoDB) {
        super(dynamoDB.getTable("munch-data.PlaceMenu"));
    }

    @Override
    protected PlaceMenu fromItem(Item item) {
        PlaceMenu menu = new PlaceMenu();
        menu.setSortKey(item.getString(sortKeyName));
        menu.setType(item.getString("type"));
        menu.setUrl(item.getString("url"));
        menu.setThumbnail(item.getMap("thumbnail"));

        menu.setSource(item.getString("source"));
        menu.setSourceId(item.getString("sourceId"));
        menu.setSourceName(item.getString("sourceName"));
        menu.setSourceUrl(item.getString("sourceUrl"));

        menu.setSourceContentTitle(item.getString("sourceContentTitle"));
        menu.setSourceContentUrl(item.getString("sourceContentUrl"));
        return menu;
    }

    @Override
    protected Item toItem(String placeId, PlaceMenu data) {
        Objects.requireNonNull(data.getSortKey());
        Objects.requireNonNull(data.getType());
        Objects.requireNonNull(data.getUrl());
        Objects.requireNonNull(data.getThumbnail());

        Item item = new Item();
        item.with(hashKeyName, placeId);
        item.with(sortKeyName, data.getSortKey());
        item.with("type", data.getType());
        item.with("url", data.getUrl());
        item.withMap("thumbnail", data.getThumbnail());

        item.with("source", data.getSource());
        item.with("sourceId", data.getSourceId());
        item.with("sourceName", data.getSourceName());
        item.with("sourceUrl", data.getSourceUrl());

        item.with("sourceContentTitle", data.getSourceContentTitle());
        item.with("sourceContentUrl", data.getSourceContentUrl());
        return item;
    }
}
