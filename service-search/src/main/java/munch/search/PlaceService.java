package munch.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import munch.data.Place;
import munch.restful.server.JsonCall;
import munch.restful.server.JsonService;
import munch.search.elastic.ElasticIndex;

/**
 * Created by: Fuxing
 * Date: 6/7/2017
 * Time: 6:36 AM
 * Project: munch-core
 */
@Singleton
public class PlaceService implements JsonService {

    private final ElasticIndex index;

    @Inject
    public PlaceService(ElasticIndex index) {
        this.index = index;
    }

    @Override
    public void route() {
        PATH("/places", () -> {
            PUT("/:cycleNo/:id", this::put);
            DELETE("/:cycleNo/before", this::deleteBefore);
            DELETE("/:cycleNo/:id", this::delete);
        });
    }

    /**
     * @param call json call
     * @return 200 = saved
     */
    private JsonNode put(JsonCall call) throws Exception {
        long cycleNo = call.pathLong("cycleNo");
        Place place = call.bodyAsObject(Place.class);
        index.put(place, cycleNo);
        return Meta200;
    }

    /**
     * @param call json call
     * @return 200 = deleted
     */
    private JsonNode delete(JsonCall call) throws Exception {
        String id = call.pathString("id");
        index.delete("place", id);
        return Meta200;
    }

    /**
     * @param call json call
     * @return 200 = deleted
     */
    private JsonNode deleteBefore(JsonCall call) throws Exception {
        long cycleNo = call.pathLong("cycleNo");
        index.deleteBefore("place", cycleNo);
        return Meta200;
    }
}
