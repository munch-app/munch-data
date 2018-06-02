package munch.data.service;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.fasterxml.jackson.databind.JsonNode;
import munch.data.elastic.ElasticIndex;
import munch.data.location.Cluster;
import munch.restful.core.JsonUtils;
import munch.restful.core.KeyUtils;
import munch.restful.server.JsonCall;
import munch.restful.server.dynamodb.RestfulDynamoHashService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 1/6/18
 * Time: 3:41 PM
 * Project: munch-data
 */
@Singleton
public final class ClusterService extends RestfulDynamoHashService<Cluster> {

    private final ElasticIndex elasticIndex;

    @Inject
    public ClusterService(DynamoDB dynamoDB, ElasticIndex elasticIndex) {
        super(dynamoDB.getTable("munch-data.Cluster"), Cluster.class, "clusterId", 100);
        this.elasticIndex = elasticIndex;
    }

    @Override
    public void route() {
        PATH("/clusters", () -> {
            GET("", this::list);
            GET("/:clusterId", this::get);

            POST("", this::post);
            PUT("/:clusterId", this::put);
            DELETE("/:clusterId", this::delete);
        });
    }

    private JsonNode post(JsonCall call) {
        Cluster cluster = call.bodyAsObject(Cluster.class);
        cluster.setClusterId(KeyUtils.randomUUIDBase64());
        cluster.setCreatedMillis(System.currentTimeMillis());
        return put(cluster);
    }

    public JsonNode put(JsonCall call) {
        Cluster cluster = call.bodyAsObject(Cluster.class);
        cluster.setClusterId(call.pathString("clusterId"));
        return put(cluster);
    }

    public Cluster delete(JsonCall call) {
        String clusterId = call.pathString("clusterId");
        elasticIndex.delete("Cluster", clusterId);
        return super.delete(clusterId);
    }

    private JsonNode put(Cluster cluster) {
        cluster.setUpdatedMillis(System.currentTimeMillis());

        elasticIndex.put(cluster);
        return super.put(cluster.getClusterId(), JsonUtils.toTree(cluster));
    }
}
