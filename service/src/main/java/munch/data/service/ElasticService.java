package munch.data.service;

import com.fasterxml.jackson.databind.JsonNode;
import munch.restful.server.JsonCall;
import munch.restful.server.JsonService;

import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 31/5/18
 * Time: 5:09 PM
 * Project: munch-data
 */
@Singleton
public final class ElasticService implements JsonService {
    @Override
    public void route() {

    }

    private JsonNode post(JsonCall call) {
        // TODO
        return Meta200;
    }
}
