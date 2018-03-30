package munch.data.place.graph.linker;

import corpus.data.CorpusData;

import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 9:29 PM
 * Project: munch-data
 */
public interface Linker {

    /**
     * @return name of linker
     */
    String getName();

    public boolean link(Map<String, Integer> matchers, CorpusData left, CorpusData right);

    // E.g. LatLng + Direct Name Match
}
