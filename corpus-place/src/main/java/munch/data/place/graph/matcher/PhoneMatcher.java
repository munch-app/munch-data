package munch.data.place.graph.matcher;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.place.elastic.ElasticClient;
import munch.data.place.graph.PlaceTree;
import munch.data.place.parser.PhoneParser;

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
    public Map<String, Integer> match(String placeId, CorpusData left, CorpusData right) {
        List<String> leftPhones = PlaceKey.phone.getAllValue(left);
        List<String> rightPhones = PlaceKey.phone.getAllValue(right);

        if (leftPhones.isEmpty() || rightPhones.isEmpty()) return Map.of();

        // TODO Parse to match, Can be negative
        return null;
    }

    @Override
    public Set<String> requiredFields() {
        return Set.of("Place.phone");
    }

    @Override
    public List<CorpusData> search(ElasticClient elasticClient, PlaceTree placeTree) {
        List<CorpusData.Field> fields = placeTree.getFields(PlaceKey.phone);
        if (fields.isEmpty()) return List.of();



        return elasticClient.search(placeTree, ElasticClient.filterTerm("Place.phone", ""));
    }

    @Override
    public void normalize(CorpusData.Field field) {
        if (!field.getKey().equals("Place.phone")) return;

        String phone = PhoneParser.normalize(field.getValue());
        if (phone != null) {
            field.setValue(phone);
        }
    }
}
