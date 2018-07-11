package munch.data.client;

import com.typesafe.config.ConfigFactory;
import munch.data.location.Area;
import munch.restful.core.NextNodeList;
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
public final class AreaClient extends RestfulDynamoHashClient<Area> {

    @Inject
    public AreaClient() {
        this(ConfigFactory.load().getString("services.munch-data.url"));
    }

    AreaClient(String url) {
        super(url, Area.class, "areaId");
    }

    public Area get(String areaId) {
        return doGet("/areas/:areaId", areaId);
    }

    public NextNodeList<Area> list(String nextAreaId, int size) {
        return doList("/areas", nextAreaId, size);
    }

    public Iterator<Area> iterator() {
        return doIterator("/areas", 20);
    }

    public Area post(Area area) {
        return doPost("/areas")
                .body(area)
                .asDataObject(Area.class);
    }

    public void put(Area area) {
        String areaId = Objects.requireNonNull(area.getAreaId());
        doPut("/areas/:areaId", areaId, area);
    }

    public Area delete(String areaId) {
        return doDelete("/areas/:areaId", areaId);
    }

    /**
     * @param areaId areaId to count
     * @return count number of places
     */
    public Long countPlaces(String areaId) {
        return doGet("/areas/:areaId/count/places")
                .path("areaId", areaId)
                .asDataObject(Long.class);
    }
}
