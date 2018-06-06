package munch.data.client;

import com.typesafe.config.ConfigFactory;
import munch.data.place.Place;
import munch.restful.client.dynamodb.NextNodeList;
import munch.restful.client.dynamodb.RestfulDynamoHashClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 2/6/18
 * Time: 6:09 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceClient extends RestfulDynamoHashClient<Place> {

    @Inject
    public PlaceClient() {
        this(ConfigFactory.load().getString("services.munch-data.url"));
    }

    PlaceClient(String url) {
        super(url, Place.class, "placeId");
    }

    public Place get(String placeId) {
        return get("/places/:placeId", placeId);
    }

    public Map<String, Place> batchGet(Collection<String> placeIds) {
        return doPost("/places/batch/get")
                .body(placeIds)
                .asDataMap(String.class, Place.class);
    }

    public <R, T> List<R> batchGet(List<T> dataList, Function<T, String> idMapper, BiFunction<T, Place, R> collector) {
        Set<String> placeIds = dataList.stream().map(idMapper).collect(Collectors.toSet());
        Map<String, Place> placeMap = batchGet(placeIds);
        return dataList.stream()
                .map(t -> {
                    String id = idMapper.apply(t);
                    Place place = placeMap.get(id);
                    return collector.apply(t, place);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public <T> void batchGetForEach(List<T> dataList, Function<T, String> idMapper, BiConsumer<T, Place> consumer) {
        List<String> placeIds = dataList.stream().map(idMapper).collect(Collectors.toList());
        Map<String, Place> placeMap = batchGet(placeIds);
        for (T data : dataList) {
            String id = idMapper.apply(data);
            consumer.accept(data, placeMap.get(id));
        }
    }

    public NextNodeList<Place> list(String nextPlaceId, int size) {
        return list("/places", nextPlaceId, size);
    }

    public Iterator<Place> list() {
        return list("/places");
    }

    public void put(Place place) {
        String placeId = Objects.requireNonNull(place.getPlaceId());
        put("/places/:placeId", placeId, place);
    }

    public Place delete(String placeId) {
        return delete("/places/:placeId", placeId);
    }
}
