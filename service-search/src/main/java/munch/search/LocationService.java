package munch.search;

import catalyst.utils.LatLngUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import munch.data.Location;
import munch.restful.core.exception.ParamException;
import munch.restful.server.JsonCall;
import munch.restful.server.JsonService;
import munch.search.elastic.ElasticClient;
import munch.search.elastic.ElasticIndex;
import munch.search.elastic.ElasticMarshaller;
import munch.search.location.LocationQuery;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 6/7/2017
 * Time: 6:36 AM
 * Project: munch-core
 */
@Singleton
public class LocationService implements JsonService {

    private final ElasticIndex index;
    private final ElasticClient client;
    private final LocationQuery locationQuery;
    private final ElasticMarshaller marshaller;

    @Inject
    public LocationService(ElasticIndex index, ElasticClient client, LocationQuery locationQuery,
                           ElasticMarshaller marshaller) {
        this.index = index;
        this.client = client;
        this.locationQuery = locationQuery;
        this.marshaller = marshaller;
    }

    @Override
    public void route() {
        PATH("/locations", () -> {
            GET("/reverse", this::reverse);
            GET("/suggest", this::suggest);

            PUT("/:cycleNo/:id", this::put);
            DELETE("/:cycleNo/before", this::deleteBefore);
            DELETE("/:cycleNo/:id", this::delete);
        });
    }

    private Location reverse(JsonCall call) throws IOException {
        LatLngUtils.LatLng latLng = parseLatLng(call.queryString("latLng"));

        JsonNode query = locationQuery.reverse(latLng.getLat(), latLng.getLng());
        JsonNode result = client.postBoolSearch("location", 0, 1, query);

        List<Location> locations = marshaller.deserializeList(result.path("hits").path("hits"));
        if (locations.isEmpty()) return null;
        return locations.get(0);
    }

    private List<Location> suggest(JsonCall call) throws IOException {
        String text = call.queryString("text").toLowerCase();
        int size = call.queryInt("size");
        if (StringUtils.isBlank(text)) return Collections.emptyList();

        // Location results search
        JsonNode results = client.suggest("location", text, null, size);
        return marshaller.deserializeList(results);
    }

    /**
     * @param call json call
     * @return 200 = saved
     */
    private JsonNode put(JsonCall call) throws Exception {
        long cycleNo = call.pathLong("cycleNo");
        Location location = call.bodyAsObject(Location.class);
        index.put(location, cycleNo);
        return Meta200;
    }

    /**
     * @param call json call
     * @return 200 = deleted
     */
    private JsonNode delete(JsonCall call) throws Exception {
        String id = call.pathString("id");
        index.delete("location", id);
        return Meta200;
    }

    /**
     * @param call json call
     * @return 200 = deleted
     */
    private JsonNode deleteBefore(JsonCall call) throws Exception {
        long cycleNo = call.pathLong("cycleNo");
        index.deleteBefore("location", cycleNo);
        return Meta200;
    }

    private static LatLngUtils.LatLng parseLatLng(String latLng) {
        try {
            return LatLngUtils.parse(latLng);
        } catch (LatLngUtils.ParseException pe) {
            throw new ParamException("latLng");
        }
    }
}
