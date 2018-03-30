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
    private final List<Matcher> matcherList;
    private final List<Searcher> searcherList;

    private final Set<String> requiredFields;
    private final ElasticClient elasticClient;

    @Inject
    public MatcherManager(ElasticClient elasticClient,
                          SpatialMatcher spatialMatcher, LocationMatcher locationMatcher,
                          PhoneMatcher phoneMatcher, NameMatcher nameMatcher) {
        this.elasticClient = elasticClient;

        this.matcherList = List.of(
                phoneMatcher,
                nameMatcher,
                spatialMatcher,
                locationMatcher
        );

        this.searcherList = List.of(
                phoneMatcher,
                spatialMatcher,
                locationMatcher
        );

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
     * @param field to normalize
     */
    public void normalizeFields(CorpusData.Field field) {
        for (Searcher searcher : searcherList) {
            searcher.normalize(field);
        }
    }

    /**
     * @param placeId is id of place
     * @param left    corpus data
     * @param right   corpus data
     * @return matcher result
     */
    public Map<String, Integer> match(String placeId, CorpusData left, CorpusData right) {
        Map<String, Integer> map = new HashMap<>();

        for (Matcher matcher : matcherList) {
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
        for (Searcher searcher : searcherList) {
            corpusDataSet.addAll(searcher.search(elasticClient, placeTree));
        }
        return corpusDataSet;
    }
}
