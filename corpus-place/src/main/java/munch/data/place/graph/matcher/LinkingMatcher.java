package munch.data.place.graph.matcher;

import corpus.data.CorpusData;

import java.util.Map;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 30/3/2018
 * Time: 11:22 PM
 * Project: munch-data
 */
public class LinkingMatcher implements Matcher {

    @Override
    public Map<String, Integer> match(String placeId, CorpusData left, CorpusData right) {
        // TODO
        return null;
    }

    @Override
    public Set<String> requiredFields() {
        return Set.of("Place.linking");
    }
}
