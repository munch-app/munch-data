package munch.data.place.graph.linker;

import corpus.data.CorpusData;
import munch.data.place.graph.PlaceTree;

import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 30/3/2018
 * Time: 11:25 PM
 * Project: munch-data
 */
public final class SpatialNameLinker implements Linker {
    @Override
    public String getName() {
        return "SpatialNameLinker";
    }

    @Override
    public boolean link(String placeId, PlaceTree left, Map<String, Integer> matchers, CorpusData right) {
        int distance = matchers.getOrDefault("Place.Location.latLng", Integer.MAX_VALUE);
        int name = matchers.getOrDefault("Place.name", 0);
        return name >= 1 && distance <= 200;
    }
}
