package munch.data.place.graph;

import corpus.data.CorpusData;
import munch.data.place.graph.matcher.MatcherManager;

import javax.inject.Inject;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 8:07 PM
 * Project: munch-data
 */
public final class PlaceElasticSearch {

    private final Set<String> requiredFields;

    /**
     * @param matcherManager matcher manager
     */
    @Inject
    public PlaceElasticSearch(MatcherManager matcherManager) {
        this.requiredFields = matcherManager.getRequiredFields();
    }

    public void put(CorpusData corpusData) {
        // TODO
    }

    public void search(String field, String value) {

        // TODO
    }
}
