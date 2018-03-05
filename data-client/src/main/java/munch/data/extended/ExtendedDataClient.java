package munch.data.extended;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.google.common.collect.Iterators;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Should be able to support:
 * - Internal
 * PlaceAward
 * PlaceMenu
 * <p>
 * - External Data
 * InstagramMedia
 * Article
 * Review (Facebook)
 * <p>
 * Created by: Fuxing
 * Date: 6/3/2018
 * Time: 12:50 AM
 * Project: munch-data
 */
abstract class ExtendedDataClient<T extends ExtendedData> {
    protected final Table table;
    protected final String hashKeyName;
    protected final String sortKeyName;

    ExtendedDataClient(Table table, String hashKeyName, String sortKeyName) {
        this.table = table;
        this.hashKeyName = hashKeyName;
        this.sortKeyName = sortKeyName;
    }

    ExtendedDataClient(Table table) {
        this(table, "p", "s");
    }

    public Iterator<T> iterator(String placeId) {
        Objects.requireNonNull(placeId);

        QuerySpec query = new QuerySpec()
                .withHashKey(hashKeyName, placeId)
                .withScanIndexForward(false);

        ItemCollection<QueryOutcome> collection = table.query(query);
        return Iterators.transform(collection.iterator(), this::fromItem);
    }

    public List<T> list(String placeId, String lastSortKey, int size) {
        Objects.requireNonNull(placeId);

        QuerySpec query = new QuerySpec()
                .withHashKey(hashKeyName, placeId)
                .withScanIndexForward(false)
                .withMaxResultSize(size);


        if (lastSortKey != null)
            query.withExclusiveStartKey(hashKeyName, placeId, sortKeyName, lastSortKey);


        ItemCollection<QueryOutcome> collection = table.query(query);
        // Collect results
        List<T> list = new ArrayList<>();
        collection.forEach(item -> list.add(fromItem(item)));
        return list;
    }

    public void put(String placeId, T data) {
        Objects.requireNonNull(placeId);
        Objects.requireNonNull(data);

        table.putItem(toItem(placeId, data));
    }

    public void delete(String placeId, String sortKey) {
        Objects.requireNonNull(placeId);
        Objects.requireNonNull(sortKey);

        table.deleteItem(hashKeyName, placeId, sortKeyName, sortKey);
    }

    @Nullable
    public T get(String placeId, String sortKey) {
        Item item = table.getItem(hashKeyName, placeId, sortKeyName, sortKey);
        if (item == null) return null;

        return fromItem(item);
    }

    /**
     * @param item item
     * @return Transformed to Data
     */
    protected abstract T fromItem(Item item);

    /**
     * @param placeId placeId nonnull
     * @param data    data nonnull
     * @return Transformed to Item
     */
    protected abstract Item toItem(String placeId, T data);
}
