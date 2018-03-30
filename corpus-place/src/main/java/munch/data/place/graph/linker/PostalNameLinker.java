package munch.data.place.graph.linker;

import corpus.data.CorpusData;

import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 30/3/2018
 * Time: 11:25 PM
 * Project: munch-data
 */
public final class PostalNameLinker implements Linker {
    @Override
    public String getName() {
        return "PostalNameLinker";
    }

    @Override
    public boolean link(Map<String, Integer> matchers, CorpusData left, CorpusData right) {
        int name = matchers.getOrDefault("Place.name", 0);
        int postal = matchers.getOrDefault("Place.Location.postal", 0);
        // City?
        return name >= 1 && postal >= 1;
    }
}
