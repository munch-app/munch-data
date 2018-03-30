package munch.data.place.graph.matcher;

import corpus.data.CorpusData;
import munch.data.place.elastic.ElasticClient;
import munch.data.place.graph.PlaceTree;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 10:04 PM
 * Project: munch-data
 */
@Singleton
public final class MatcherManager {
    private final List<Matcher> matcherList = List.of(
            new PlaceIdMatcher(),
            new PhoneMatcher()
    );

    private final List<Searcher> searcherList = List.of(
            new PhoneMatcher()
    );

    private final Set<String> requiredFields;

    private final ElasticClient elasticClient;

    @Inject
    public MatcherManager(ElasticClient elasticClient) {
        this.elasticClient = elasticClient;
        this.requiredFields = matcherList.stream()
                .flatMap(matcher -> matcher.requiredFields().stream())
                .collect(Collectors.toSet());
    }

    /**
     * @return fields required for matcher
     */
    public Set<String> getRequiredFields() {
        return requiredFields;
    }

    /**
     * @param left  corpus data
     * @param right corpus data
     * @return matcher result
     */
    public Map<String, Integer> match(CorpusData left, CorpusData right) {
        Map<String, Integer> map = new HashMap<>();

        for (Matcher matcher : matcherList) {
            map.putAll(matcher.match(left, right));
        }
        return map;
    }

    /**
     * @param placeTree place tree
     * @return List of matched corpus data from elastic search
     */
    public Set<CorpusData> search(PlaceTree placeTree) {
        Set<CorpusData> corpusDataSet = new HashSet<>();
        for (Searcher searcher : searcherList) {
            corpusDataSet.addAll(searcher.search(elasticClient, placeTree));
        }
        return corpusDataSet;
    }
}
