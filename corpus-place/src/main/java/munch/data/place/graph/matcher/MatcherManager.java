package munch.data.place.graph.matcher;

import corpus.data.CorpusData;
import munch.data.place.elastic.ElasticClient;
import munch.data.place.graph.PlaceTree;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 10:04 PM
 * Project: munch-data
 */
@Singleton
public final class MatcherManager {
    private final Set<Matcher> matchers;
    private final Set<Searcher> searchers;

    private final ElasticClient elasticClient;

    @Inject
    public MatcherManager(ElasticClient elasticClient, Set<Matcher> matchers, Set<Searcher> searchers) {
        this.elasticClient = elasticClient;
        this.matchers = matchers;
        this.searchers = searchers;
    }

    /**
     * @param placeId is id of place
     * @param left    corpus data
     * @param right   corpus data
     * @return matcher result
     */
    public Map<String, Integer> match(String placeId, CorpusData left, CorpusData right) {
        Map<String, Integer> map = new HashMap<>();

        for (Matcher matcher : matchers) {
            map.putAll(matcher.match(placeId, left, right));
        }
        return map;
    }

    /**
     * @param placeTree place tree
     * @return List of matched corpus data from elastic search
     */
    public Set<CorpusData> search(PlaceTree placeTree) {
        Set<CorpusData> corpusDataSet = new HashSet<>();
        for (Searcher searcher : searchers) {
            corpusDataSet.addAll(searcher.search(elasticClient, placeTree));
        }
        return corpusDataSet;
    }
}
