package munch.data.place.graph.matcher;

import corpus.data.CorpusData;
import corpus.field.AbstractKey;
import corpus.field.PlaceKey;
import corpus.utils.FieldCollector;
import munch.data.place.elastic.ElasticClient;
import munch.data.place.graph.PlaceTree;

import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 30/3/2018
 * Time: 10:29 PM
 * Project: munch-data
 */
@Singleton
public final class LocationMatcher implements Matcher, Searcher {

    @Override
    public Map<String, Integer> match(String placeId, CorpusData left, CorpusData right) {
        return Map.of(
                "Place.Location.postal", matchPostal(left, right) ? 1 : 0,
                "Place.Location.unitNumber", matchField(PlaceKey.Location.unitNumber, left, right) ? 1 : 0,
                "Place.Location.street", matchField(PlaceKey.Location.street, left, right) ? 1 : 0,
                "Place.Location.address", matchField(PlaceKey.Location.address, left, right) ? 1 : 0
        );
    }

    private static boolean matchField(AbstractKey key, CorpusData left, CorpusData right) {
        String leftValue = key.getValue(left);
        if (leftValue == null) return false;

        return leftValue.equalsIgnoreCase(key.getValue(right));
    }

    private static boolean matchPostal(CorpusData left, CorpusData right) {
        String leftPostal = PlaceKey.Location.postal.getValue(left);
        if (leftPostal == null) return false;


        String rightPostal = PlaceKey.Location.postal.getValue(right);
        if (rightPostal == null) return false;

        return fixPostal(leftPostal).equals(fixPostal(rightPostal));
    }

    @Override
    public Set<String> requiredFields() {
        return Set.of(
                "Place.Location.postal",
                "Place.Location.unitNumber",
                "Place.Location.street",
                "Place.Location.address"
        );
    }

    @Override
    public List<CorpusData> search(ElasticClient elasticClient, PlaceTree placeTree) {
        FieldCollector postalCollector = placeTree.getFieldCollector(PlaceKey.Location.postal);
        String postal = postalCollector.collectMax();
        if (postal == null) return List.of();

        return elasticClient.search(placeTree, ElasticClient.filterTerm("Place.Location.postal", postal));
    }

    @Override
    public void normalize(CorpusData.Field field) {
        if (!field.getKey().equals("Place.Location.postal")) return;

        field.setValue(fixPostal(field.getValue()));
    }

    private static String fixPostal(String postal) {
        if (postal != null && postal.length() == 5) return "0" + postal;
        return postal;
    }
}
