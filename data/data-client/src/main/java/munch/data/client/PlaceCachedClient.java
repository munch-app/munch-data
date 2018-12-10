package munch.data.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import munch.data.place.Place;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by: Fuxing
 * Date: 19/10/18
 * Time: 3:21 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceCachedClient {
    private final PlaceClient placeClient;

    private final LoadingCache<String, Optional<Place>> loadingCache;

    @Inject
    public PlaceCachedClient(PlaceClient placeClient) {
        this(placeClient, 1000, 30, TimeUnit.MINUTES);
    }

    /**
     * @param placeClient PlaceClient
     * @param size        max size in cache
     * @param duration    time unit duration
     * @param timeUnit    time unit
     */
    public PlaceCachedClient(PlaceClient placeClient, int size, int duration, TimeUnit timeUnit) {
        this.placeClient = placeClient;
        this.loadingCache = CacheBuilder.newBuilder()
                .maximumSize(size)
                .expireAfterWrite(duration, timeUnit)
                .build(CacheLoader.from(placeId -> Optional.ofNullable(placeClient.get(placeId))));
    }

    /**
     * @param placeIds to batch get
     * @return Map of PlaceId -> Place
     */
    public Map<String, Place> get(Stream<String> placeIds) {
        return get(placeIds.collect(Collectors.toSet()));
    }

    /**
     * @param placeIds to batch get
     * @return Map of PlaceId -> Place
     */
    public Map<String, Place> get(Collection<String> placeIds) {
        // Fetch from loading cache first
        ImmutableMap<String, Optional<Place>> present = loadingCache.getAllPresent(placeIds);
        Set<String> collected = new HashSet<>(placeIds);
        present.forEach((s, place) -> collected.remove(s));

        // Batch get and persist into loading cache
        Map<String, Place> batch = placeClient.batchGet(collected);
        batch.forEach((s, place) -> loadingCache.put(s, Optional.ofNullable(place)));

        // Join and return
        Map<String, Place> mapped = new HashMap<>(batch);
        present.forEach((s, place) -> mapped.put(s, place.orElse(null)));
        return mapped;
    }

    /**
     * @param placeId to get
     * @return Place or Null
     */
    @Nullable
    public Place get(String placeId) {
        return loadingCache.getUnchecked(placeId)
                .orElse(null);
    }
}
