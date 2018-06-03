package munch.data.service;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.fasterxml.jackson.databind.JsonNode;
import munch.data.elastic.ElasticIndex;
import munch.data.location.Landmark;
import munch.restful.core.JsonUtils;
import munch.restful.core.KeyUtils;
import munch.restful.server.JsonCall;
import munch.restful.server.dynamodb.RestfulDynamoHashService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 1/6/18
 * Time: 1:08 PM
 * Project: munch-data
 */
@Singleton
public final class LandmarkService extends RestfulDynamoHashService<Landmark> {

    private final ElasticIndex elasticIndex;

    @Inject
    protected LandmarkService(DynamoDB dynamoDB, ElasticIndex elasticIndex) {
        super(dynamoDB.getTable("munch-data.Landmark"), Landmark.class, "landmarkId", 100);
        this.elasticIndex = elasticIndex;
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

    private JsonNode post(JsonCall call) {
        Landmark landmark = call.bodyAsObject(Landmark.class);
        landmark.setLandmarkId(KeyUtils.randomUUIDBase64());
        landmark.setCreatedMillis(System.currentTimeMillis());
        return put(landmark);
    }

    public JsonNode put(JsonCall call) {
        Landmark landmark = call.bodyAsObject(Landmark.class);
        landmark.setLandmarkId(call.pathString("landmarkId"));
        return put(landmark);
    }

    public Landmark delete(JsonCall call) {
        String landmarkId = call.pathString("landmarkId");
        elasticIndex.delete("Landmark", landmarkId);
        return super.delete(landmarkId);
    }

    private JsonNode put(Landmark landmark) {
        landmark.setUpdatedMillis(System.currentTimeMillis());

        elasticIndex.put(landmark);
        super.put(landmark.getLandmarkId(), JsonUtils.toTree(landmark));
        return nodes(200, landmark);
    }
}
