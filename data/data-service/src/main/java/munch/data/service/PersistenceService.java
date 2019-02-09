package munch.data.service;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import munch.data.ElasticObject;
import munch.data.elastic.ElasticIndex;
import munch.restful.core.JsonUtils;
import munch.restful.core.exception.ParamException;
import munch.restful.server.JsonResult;
import munch.restful.server.dynamodb.RestfulDynamoHashService;

/**
 * Created by: Fuxing
 * Date: 4/6/18
 * Time: 8:28 PM
 * Project: munch-data
 */
public abstract class PersistenceService<T extends ElasticObject> extends RestfulDynamoHashService<T> {

    protected final String dataType;
    protected final ElasticIndex elasticIndex;

    public PersistenceService(PersistenceMapping persistenceMapping, ElasticIndex elasticIndex, Class<T> clazz) {
        super(persistenceMapping.getMapping(clazz).getTable(), clazz, persistenceMapping.getMapping(clazz).getDataKey(), 100);
        this.dataType = persistenceMapping.getMapping(clazz).getDataType();
        this.elasticIndex = elasticIndex;
    }

    @Override
    public JsonResult put(Object hash, JsonNode json) {
        ParamException.requireNonNull(hashName, hash);
        ((ObjectNode) json).putPOJO(hashName, hash);

        // Convert to Object class to validation against Class Type
        T object = JsonUtils.toObject(json, clazz);
        put(object);
        return result(200);
    }

    public T put(T object) {
        object.setUpdatedMillis(System.currentTimeMillis());

        // Updated & Created Millis is created via PersistenceService
        T old = get(object.getDataId());
        if (old != null) object.setCreatedMillis(old.getCreatedMillis());
        else object.setCreatedMillis(object.getUpdatedMillis());

        validate(object);
        elasticIndex.put(object);

        // Convert validated object back to json to remove unwanted fields
        table.putItem(Item.fromJSON(JsonUtils.toString(object)));
        return object;
    }

    @Override
    protected T delete(Object hashValue) {
        elasticIndex.delete(dataType, (String) hashValue);
        return super.delete(hashValue);
    }
}
