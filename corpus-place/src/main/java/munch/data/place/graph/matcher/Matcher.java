package munch.data.place.graph.matcher;

import corpus.data.CorpusData;

import java.util.Map;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 8:12 PM
 * Project: munch-data
 */
public interface Matcher {

    /**
     * @param left  corpus data
     * @param right corpus data
     * @return Map of score, can be negative
     */
    Map<String, Integer> match(CorpusData left, CorpusData right);

    /**
     * @return fields required for matching
     */
    Set<String> requiredFields();
}
