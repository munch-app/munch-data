package munch.data.clients;

import com.typesafe.config.Config;
import munch.data.structure.Place;
import munch.data.structure.SearchQuery;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 10/10/2017
 * Time: 1:57 AM
 * Project: munch-data
 */
@Singleton
public class PlaceClient {

    @Inject
    public PlaceClient(Config config) {
        // Search and DynamoDB
    }

    /**
     * @param query query object
     * @return List of Place result
     * @see SearchQuery
     */
    public List<Place> search(SearchQuery query) {
        return null;
    }

    public Place get(String id) {
        return null;
    }


    public void put(Place place) {

    }

    public void delete(String id) {

    }
}
