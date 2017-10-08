package munch.search;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import munch.restful.server.RestfulServer;

/**
 * Created by: Fuxing
 * Date: 6/7/2017
 * Time: 6:17 AM
 * Project: munch-core
 */
@Singleton
public class SearchApi extends RestfulServer {

    @Inject
    public SearchApi(HealthService health,
                     SearchService search,
                     PlaceService place,
                     LocationService location,
                     TagService tag) {
        super(health, search, place, location, tag);
    }
}
