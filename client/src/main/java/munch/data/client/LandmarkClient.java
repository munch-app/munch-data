package munch.data.client;

import com.typesafe.config.ConfigFactory;
import munch.data.location.Landmark;
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
public final class LandmarkClient extends RestfulDynamoHashClient<Landmark> {

    @Inject
    public LandmarkClient() {
        this(ConfigFactory.load().getString("services.munch-data.url"));
    }

    LandmarkClient(String url) {
        super(url, Landmark.class, "landmarkId");
    }

    public Landmark get(String landmarkId) {
        return get("/landmarks/{landmarkId}", landmarkId);
    }

    public NextNodeList<Landmark> list(String nextLandmarkId, int size) {
        return list("/landmarks", nextLandmarkId, size);
    }

    public Iterator<Landmark> list() {
        return list("/landmarks");
    }

    public Landmark post(Landmark landmark) {
        return doPost("/landmarks")
                .body(landmark)
                .asDataObject(Landmark.class);
    }

    public void put(Landmark landmark) {
        String landmarkId = Objects.requireNonNull(landmark.getLandmarkId());
        put("/landmarks/{landmarkId}", landmarkId, landmark);
    }

    public Landmark delete(String landmarkId) {
        return delete("/landmarks/{landmarkId}", landmarkId);
    }
}
