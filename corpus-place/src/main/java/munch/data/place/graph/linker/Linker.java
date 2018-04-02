package munch.data.place.graph.linker;

import corpus.data.CorpusData;
import munch.data.place.graph.PlaceTree;

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

    boolean link(String placeId, PlaceTree left, Map<String, Integer> matchers, CorpusData right);
}
