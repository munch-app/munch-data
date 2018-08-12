package munch.data.client;

import com.typesafe.config.ConfigFactory;
import munch.data.place.PlaceAward;
import munch.restful.client.dynamodb.RestfulDynamoHashRangeClient;
import munch.restful.core.NextNodeList;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Iterator;

/**
 * Created by: Fuxing
 * Date: 11/8/18
 * Time: 6:43 AM
 * Project: munch-data
 */
@Singleton
public final class PlaceAwardClient extends RestfulDynamoHashRangeClient<PlaceAward> {

    @Inject
    public PlaceAwardClient() {
        this(ConfigFactory.load().getString("services.munch-data.url"));
    }

    public PlaceAwardClient(String url) {
        super(url, PlaceAward.class, "placeId", "awardId");
    }

    /**
     * @param placeId id of Place
     * @param size    per page to fetch
     * @return Iterator of PlaceAward
     */
    public Iterator<PlaceAward> iterator(String placeId, int size) {
        return doIterator("/places/:placeId/awards", placeId, size);
    }

    public NextNodeList<PlaceAward> list(String placeId, @Nullable Long nextSort, int size) {
        return doList("/places/:placeId/awards", placeId, nextSort, size);
    }

    public PlaceAward get(String placeId, String awardId) {
        return doGet("/places/:placeId/awards/:awardId", placeId, awardId);
    }

    public void put(String placeId, String awardId, PlaceAward placeAward) {
        doPut("/places/:placeId/awards/:awardId", placeId, awardId, placeAward);
    }

    public PlaceAward delete(String placeId, String awardId) {
        return doDelete("/places/:placeId/awards/:awardId", placeId, awardId);
    }
}
