package munch.collections;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.internal.ItemValueConformer;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.databind.ObjectMapper;
import munch.restful.core.JsonUtils;
import munch.restful.core.exception.JsonException;
import munch.restful.core.exception.ParamException;
import munch.restful.core.exception.ValidationException;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 9/1/18
 * Time: 3:38 PM
 * Project: munch-core
 */
@Singleton
public final class CollectionClient {
    private static final Pattern PATTERN_UUID = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    private static final String DYNAMO_TABLE_NAME = "munch-core.PlaceCollection";

    private static final ItemValueConformer valueConformer = new ItemValueConformer();
    private static final ObjectMapper objectMapper = JsonUtils.objectMapper;

    private final Table table;
    private final Index sortIndex;

    @Inject
    public CollectionClient(DynamoDB dynamoDB) {
        this.table = dynamoDB.getTable(DYNAMO_TABLE_NAME);
        this.sortIndex = table.getIndex("sortKey-index");
    }

    /**
     * Leaky Bucket or similar solution should be added on other endpoint to either prevent too many collections or abuse of system
     *
     * @param collection collection to add
     */
    public void put(PlaceCollection collection) {
        Objects.requireNonNull(collection.getUserId());
        Objects.requireNonNull(collection.getName());

        // Optional Inject
        if (StringUtils.isBlank(collection.getCollectionId())) collection.setCollectionId(UUID.randomUUID().toString());
        if (collection.getSortKey() == 0) collection.setSortKey(System.currentTimeMillis());
        if (collection.getCreatedDate() == null) collection.setCreatedDate(new Date());

        // Mandatory Inject
        collection.setUpdatedDate(new Date());

        // Validate PlaceCollection based on bean validator
        ValidationException.validate(collection);


        List<AttributeUpdate> attributeUpdateList = new ArrayList<>();
        attributeUpdateList.add(new AttributeUpdate("s").put(collection.getSortKey()));
        attributeUpdateList.add(new AttributeUpdate("n").put(collection.getName()));

        if (collection.getDescription() != null) {
            attributeUpdateList.add(new AttributeUpdate("d").put(collection.getDescription()));
        } else {
            attributeUpdateList.add(new AttributeUpdate("d").delete());
        }

        if (collection.getCount() != null) {
            attributeUpdateList.add(new AttributeUpdate("pc").put(collection.getCount()));
        }
        if (collection.getThumbnail() != null) {
            attributeUpdateList.add(new AttributeUpdate("t").put(writeValue(collection.getThumbnail())));
        }

        attributeUpdateList.add(new AttributeUpdate("ud").put(System.currentTimeMillis()));
        attributeUpdateList.add(new AttributeUpdate("cd").put(collection.getCreatedDate().getTime()));

        table.updateItem(new UpdateItemSpec()
                .withPrimaryKey("u", collection.getUserId(), "c", collection.getCollectionId())
                .withAttributeUpdate(attributeUpdateList));
    }

    public void delete(String userId, String collectionId) {
        Objects.requireNonNull(userId);
        validateUUID(collectionId, "collectionId");
        table.deleteItem("u", userId, "c", collectionId);
    }

    public PlaceCollection get(String userId, String collectionId) {
        Objects.requireNonNull(userId);
        validateUUID(collectionId, "collectionId");

        Item item = table.getItem("u", userId, "c", collectionId);
        if (item == null) return null;
        return fromItem(item);
    }

    public List<PlaceCollection> list(String userId, @Nullable Long maxSortKey, int size) {
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
        List<PlaceCollection> collections = new ArrayList<>();
        collection.forEach(item -> collections.add(fromItem(item)));
        return collections;
    }

    private PlaceCollection fromItem(Item item) {
        PlaceCollection collection = new PlaceCollection();
        collection.setUserId(item.getString("u"));
        collection.setCollectionId(item.getString("c"));
        collection.setSortKey(item.getLong("s"));

        collection.setName(item.getString("n"));
        collection.setDescription(item.getString("d"));
        if (item.hasAttribute("pc")) {
            collection.setCount(item.getLong("pc"));
        }else {
            collection.setCount(0L);
        }

        collection.setThumbnail(item.getMap("t"));

        collection.setUpdatedDate(new Date(item.getLong("ud")));
        collection.setCreatedDate(new Date(item.getLong("cd")));
        return collection;
    }

    public static void validateUUID(String id, String type) {
        Objects.requireNonNull(id, "Id Requires non null");
        if (PATTERN_UUID.matcher(id).matches()) return;
        throw new ParamException("Failed Id Validation for " + type);
    }

    private static Object writeValue(Object object) {
        try {
            if (object == null) return null;
            String json = objectMapper.writeValueAsString(object);
            return valueConformer.transform(Jackson.fromJsonString(json, Object.class));
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }
}
