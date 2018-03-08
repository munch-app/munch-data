package munch.data.place.group;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Created by: Fuxing
 * Date: 8/3/18
 * Time: 6:00 PM
 * Project: munch-data
 */
public final class PlaceTagCounter {
    private final Map<String, Map<String, Integer>> mapping = new HashMap<>();

    public void increment(String tag, String name) {
        mapping.compute(tag, (s, map) -> {
            if (map == null) map = new HashMap<>();
            map.compute(name, (s1, integer) -> {
                if (integer == null) integer = 0;
                return ++integer;
            });
            return map;
        });
    }

    public void forEach(BiConsumer<String, Map<String, Integer>> consumer) {
        mapping.forEach(consumer);
    }
}
