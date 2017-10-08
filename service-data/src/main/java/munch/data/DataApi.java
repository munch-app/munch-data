package munch.data;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import munch.restful.server.RestfulServer;

/**
 * Created by: Fuxing
 * Date: 3/7/2017
 * Time: 11:24 PM
 * Project: munch-core
 */
@Singleton
final class DataApi extends RestfulServer {

    @Inject
    public DataApi(HealthService health, PlaceService place, ArticleService article) {
        super(health, place, article);
    }
}
