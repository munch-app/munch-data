package munch.data.clients;

import com.google.inject.Singleton;
import com.typesafe.config.Config;
import munch.data.Location;
import munch.data.Place;
import munch.data.Tag;
import munch.restful.client.RestfulClient;

import javax.inject.Inject;

/**
 * Created by: Fuxing
 * Date: 9/7/2017
 * Time: 4:23 AM
 * Project: munch-core
 */
@Singleton
public class SearchClient extends RestfulClient {

    @Inject
    public SearchClient(Config config) {
        super(config.getString("services.search.url"));
    }

    public void put(Place place, long cycleNo) {
        doPut("/places/{cycleNo}/{id}")
                .path("cycleNo", cycleNo)
                .path("id", place.getId())
                .body(place)
                .asResponse()
                .hasCode(200);
    }

    public void put(Location location, long cycleNo) {
        doPut("/locations/{cycleNo}/{id}")
                .path("cycleNo", cycleNo)
                .path("id", location.getId())
                .body(location)
                .asResponse()
                .hasCode(200);
    }

    public void put(Tag tag, long cycleNo) {
        doPut("/tags/{cycleNo}/{id}")
                .path("cycleNo", cycleNo)
                .path("id", tag.getId())
                .body(tag)
                .asResponse()
                .hasCode(200);
    }

    public void deletePlaces(long cycleNo) {
        doDelete("/places/{cycleNo}/before")
                .path("cycleNo", cycleNo)
                .asResponse()
                .hasCode(200);

    }

    public void deleteLocations(long cycleNo) {
        doDelete("/locations/{cycleNo}/before")
                .path("cycleNo", cycleNo)
                .asResponse()
                .hasCode(200);
    }

    public void deleteTags(long cycleNo) {
        doDelete("/tags/{cycleNo}/before")
                .path("cycleNo", cycleNo)
                .asResponse()
                .hasCode(200);
    }
}
