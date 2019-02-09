package munch.data.service;

import munch.data.elastic.ElasticIndex;
import munch.data.location.Landmark;
import munch.restful.core.KeyUtils;
import munch.restful.server.JsonCall;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 1/6/18
 * Time: 1:08 PM
 * Project: munch-data
 */
@Singleton
public final class LandmarkService extends PersistenceService<Landmark> {

    @Inject
    public LandmarkService(PersistenceMapping persistenceMapping, ElasticIndex elasticIndex) {
        super(persistenceMapping, elasticIndex, Landmark.class);
    }

    @Override
    public void route() {
        PATH("/landmarks", () -> {
            GET("", this::list);
            GET("/:landmarkId", this::get);

            POST("", this::post);
            PUT("/:landmarkId", this::put);
            DELETE("/:landmarkId", this::delete);
        });
    }

    private Landmark post(JsonCall call) {
        Landmark landmark = call.bodyAsObject(Landmark.class);
        landmark.setLandmarkId(KeyUtils.randomUUID());
        return put(landmark);
    }
}
