package munch.data.client;

import com.typesafe.config.ConfigFactory;
import munch.data.location.Cluster;
import munch.restful.client.dynamodb.NextNodeList;
import munch.restful.client.dynamodb.RestfulDynamoHashClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 2/6/18
 * Time: 6:10 PM
 * Project: munch-data
 */
@Singleton
public final class ClusterClient extends RestfulDynamoHashClient<Cluster> {

    @Inject
    public ClusterClient() {
        this(ConfigFactory.load().getString("services.munch-data.url"));
    }

    ClusterClient(String url) {
        super(url, Cluster.class, "clusterId");
    }

    public Cluster get(String clusterId) {
        return get("/clusters/{clusterId}", clusterId);
    }

    public NextNodeList<Cluster> list(String nextClusterId, int size) {
        return list("/clusters", nextClusterId, size);
    }

    public Iterator<Cluster> list() {
        return list("/clusters");
    }

    public Cluster post(Cluster cluster) {
        return doPost("/clusters")
                .body(cluster)
                .asDataObject(Cluster.class);
    }

    public void put(Cluster cluster) {
        String clusterId = Objects.requireNonNull(cluster.getClusterId());
        put("/clusters/{clusterId}", clusterId, cluster);
    }

    public Cluster delete(String clusterId) {
        return delete("/clusters/{clusterId}", clusterId);
    }
}
