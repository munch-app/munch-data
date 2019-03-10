package munch.data.service;

import com.fasterxml.jackson.databind.JsonNode;
import munch.data.ElasticObject;
import munch.data.elastic.ElasticIndex;
import munch.restful.core.exception.CodeException;
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
        // Use v5 endpoint
        throw new CodeException(409);
    }

    public T put(T object) {
        // Use v5 endpoint
        throw new CodeException(409);
    }

    @Override
    protected T delete(Object hashValue) {
        elasticIndex.delete(dataType, (String) hashValue);
        return super.delete(hashValue);
    }
}
