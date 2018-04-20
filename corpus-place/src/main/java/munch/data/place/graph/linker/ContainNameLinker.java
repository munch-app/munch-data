package munch.data.place.graph.linker;

import corpus.data.CorpusData;
import munch.data.place.graph.PlaceTree;

import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 20/4/2018
 * Time: 8:53 AM
 * Project: munch-data
 */
public final class ContainNameLinker implements Linker {
    @Override
    public String getName() {
        return "ContainNameLinker";
    }

    @Override
    public boolean link(String placeId, PlaceTree left, Map<String, Integer> matchers, CorpusData right) {
        int postal = matchers.getOrDefault("Place.Location.postal", 0);
        int unit = matchers.getOrDefault("Place.Location.unitNumber", 0);
        int contains = matchers.getOrDefault("Place.name.contains", 0);

        return contains >= 1 && postal >= 1 && unit >= 1;
    }
}
