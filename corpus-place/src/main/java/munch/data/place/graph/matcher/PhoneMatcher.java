package munch.data.place.graph.matcher;

import corpus.data.CorpusData;
import munch.data.place.elastic.ElasticClient;
import munch.data.place.graph.PlaceTree;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 11:10 PM
 * Project: munch-data
 */
public class PhoneMatcher implements Matcher, Searcher {

    @Override
    public Map<String, Integer> match(CorpusData left, CorpusData right) {
        // TODO Parse to match, Can be negative
        return null;
    }

    @Override
    public Set<String> requiredFields() {
        return Set.of("Place.phone");
    }

    @Override
    public List<CorpusData> search(ElasticClient elasticClient, PlaceTree placeTree) {
        // TODO find phone in placeTree
        return null;
    }
}
