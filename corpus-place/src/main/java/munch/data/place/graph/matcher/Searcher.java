package munch.data.place.graph.matcher;

import corpus.data.CorpusData;
import munch.data.place.graph.PlaceElasticSearch;
import munch.data.place.graph.PlaceTree;

import java.util.List;

/**
 * Searcher is a version of Matcher that is searchable in elastic
 * <p>
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 8:13 PM
 * Project: munch-data
 */
public interface Searcher extends Matcher {

    /**
     * @param elasticSearch to use
     * @param placeTree     to search from
     * @return searched result, fields are trimmed
     */
    List<CorpusData> search(PlaceElasticSearch elasticSearch, PlaceTree placeTree);
}
