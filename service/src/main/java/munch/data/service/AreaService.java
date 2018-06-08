package munch.data.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.searchbox.client.JestClient;
import io.searchbox.core.Count;
import munch.data.elastic.ElasticIndex;
import munch.data.elastic.ElasticMapping;
import munch.data.elastic.ElasticUtils;
import munch.data.exception.ElasticException;
import munch.data.location.Area;
import munch.restful.core.JsonUtils;
import munch.restful.core.KeyUtils;
import munch.restful.server.JsonCall;
import munch.restful.server.JsonResult;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 1/6/18
 * Time: 3:41 PM
 * Project: munch-data
 */
@Singleton
public final class AreaService extends PersistenceService<Area> {

    private final JestClient client;
    private final ClusterManager clusterManager;

    @Inject
    public AreaService(PersistenceMapping persistenceMapping, ElasticIndex elasticIndex, JestClient client, ClusterManager clusterManager) {
        super(persistenceMapping, elasticIndex, Area.class);
        this.client = client;
        this.clusterManager = clusterManager;
    }

    @Override
    public void route() {
        PATH("/areas", () -> {
            GET("", this::list);
            GET("/:areaId", this::get);
            GET("/:areaId/count/places", this::countPlaces);

            POST("", this::post);
            PUT("/:areaId", this::put);
            DELETE("/:areaId", this::delete);
        });
    }

    private JsonResult post(JsonCall call) {
        Area area = call.bodyAsObject(Area.class);
        area.setAreaId(KeyUtils.randomUUID());
        return put(area);
    }

    @Override
    protected JsonResult put(Area object) {
        clusterManager.update(object);
        return super.put(object);
    }

    @Override
    protected Area delete(Object hashValue) {
        Area area = super.delete(hashValue);
        if (area != null) clusterManager.delete(area);
        return area;
    }

    private JsonResult countPlaces(JsonCall call) {
        ObjectNode root = JsonUtils.createObjectNode();
        root.put("from", 0);

        ObjectNode bool = JsonUtils.createObjectNode();
        bool.set("must", ElasticUtils.mustMatchAll());
        bool.set("filter", JsonUtils.createArrayNode()
                .add(ElasticUtils.filterTerm("dataType", "Place"))
                .add(ElasticUtils.filterTerms("areas.areaId", List.of(call.pathString(hashName))))
        );
        root.putObject("query").set("bool", bool);

        Count count = new Count.Builder()
                .addIndex(ElasticMapping.INDEX_NAME)
                .query(JsonUtils.toString(root))
                .build();

        try {
            Double number = client.execute(count).getCount();
            if (number == null) return result(200, 0);
            return result(200, number.longValue());
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }
}
