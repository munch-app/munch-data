package munch.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.munch.hibernate.utils.TransactionProvider;
import munch.data.database.AbstractEntity;
import munch.restful.server.JsonCall;
import munch.restful.server.JsonService;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 16/8/2017
 * Time: 9:23 PM
 * Project: munch-core
 */
public abstract class AbstractService<R, T extends AbstractEntity<R>> implements JsonService {
    protected final String serviceName;

    @Inject
    protected TransactionProvider provider;

    protected final Class<R> dataClass;
    protected final Class<T> entityClass;
    protected final String entityName;

    protected AbstractService(String serviceName, Class<R> dataClass, Class<T> entityClass) {
        this.serviceName = serviceName;
        this.dataClass = dataClass;
        this.entityClass = entityClass;
        this.entityName = entityClass.getSimpleName();
    }

    @Override
    public void route() {
        PATH(serviceName, () -> {
            POST("/get", this::batchGet);
            GET("/:id", this::get);
            PUT("/:cycleNo/:id", this::put);
            DELETE("/:cycleNo/before", this::deleteBefore);
        });
    }

    protected abstract T newEntity(R data, long cycleNo);

    protected abstract Function<T, String> getKeyMapper();

    protected abstract List<T> getList(List<String> keys);

    protected List<R> batchGet(JsonCall call) {
        List<String> keys = call.bodyAsList(String.class);

        if (keys.isEmpty()) return Collections.emptyList();

        Map<String, T> placeMap = getList(keys).stream()
                .collect(Collectors.toMap(getKeyMapper(), Function.identity()));
        return keys.stream()
                .map(placeMap::get)
                .map(AbstractEntity::getData)
                .collect(Collectors.toList());
    }

    protected R get(JsonCall call) {
        String id = call.pathString("id");

        return provider.optional(em -> em.find(entityClass, id))
                .map(AbstractEntity::getData)
                .orElse(null);
    }

    protected JsonNode put(JsonCall call) {
        long cycleNo = call.pathLong("cycleNo");

        R data = call.bodyAsObject(dataClass);
        T entity = newEntity(data, cycleNo);
        provider.with(em -> em.merge(entity));
        return Meta200;
    }

    private JsonNode deleteBefore(JsonCall call) {
        long cycleNo = call.pathLong("cycleNo");

        provider.with(em -> em.createQuery(
                "DELETE FROM " + entityName + " WHERE cycleNo < :cycleNo")
                .setParameter("cycleNo", cycleNo)
                .executeUpdate());
        return Meta200;
    }
}
