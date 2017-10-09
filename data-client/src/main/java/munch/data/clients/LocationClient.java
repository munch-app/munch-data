package munch.data.clients;

import com.typesafe.config.Config;
import munch.data.structure.Location;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 10/10/2017
 * Time: 2:16 AM
 * Project: munch-data
 */
@Singleton
public class LocationClient {

    @Inject
    public LocationClient(Config config) {
        // Search and DynamoDB
    }

    /**
     * @param text text
     * @param size size of location to suggest
     * @return list of Location
     */
    public List<Location> suggest(String text, int size) {
        return null;
    }

    public Location get(String id) {
        return null;
    }


    public void put(Location location) {

    }

    public void delete(String id) {

    }
}
