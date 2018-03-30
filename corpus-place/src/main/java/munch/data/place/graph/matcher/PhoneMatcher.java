package munch.data.place.graph.matcher;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import corpus.utils.FieldCollector;
import munch.data.place.elastic.ElasticClient;
import munch.data.place.graph.PlaceTree;
import munch.data.place.parser.PhoneParser;

import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 11:10 PM
 * Project: munch-data
 */
@Singleton
public final class PhoneMatcher implements Matcher, Searcher {

    @Override
    public Map<String, Integer> match(String placeId, CorpusData left, CorpusData right) {
        List<String> leftPhones = PlaceKey.phone.getAllValue(left).stream()
                .map(PhoneParser::normalize)
                .collect(Collectors.toList());
        if (leftPhones.isEmpty()) return Map.of();

        List<String> rightPhones = PlaceKey.phone.getAllValue(right).stream()
                .map(PhoneParser::normalize)
                .collect(Collectors.toList());

        if (rightPhones.isEmpty()) return Map.of();

        for (String rightPhone : rightPhones) {
            if (!leftPhones.contains(rightPhone)) {
                return Map.of("Place.phone", -1);
            }
        }
        return Map.of("Place.phone", 1);
    }

    @Override
    public Set<String> requiredFields() {
        return Set.of("Place.phone");
    }

    @Override
    public List<CorpusData> search(ElasticClient elasticClient, PlaceTree placeTree) {
        FieldCollector fieldCollector = placeTree.getFieldCollector(PlaceKey.phone);
        String phone = fieldCollector.collectMax();
        if (phone == null) return List.of();

        return elasticClient.search(placeTree, ElasticClient.filterTerm("Place.phone", phone));
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
