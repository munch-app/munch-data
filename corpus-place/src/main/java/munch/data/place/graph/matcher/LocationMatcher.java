package munch.data.place.graph.matcher;

import corpus.data.CorpusData;
import corpus.field.AbstractKey;
import corpus.field.PlaceKey;
import corpus.utils.FieldCollector;
import munch.data.place.elastic.ElasticClient;
import munch.data.place.graph.PlaceTree;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 30/3/2018
 * Time: 10:29 PM
 * Project: munch-data
 */
@Singleton
public final class LocationMatcher implements Matcher, Searcher {
    private static final Pattern UNIT_SPLIT_PATTERN = Pattern.compile("#?0?(?<left>[^-]+)(-0?(?<right>.*))?");

    @Override
    public Map<String, Integer> match(String placeId, CorpusData left, CorpusData right) {
        return Map.of(
                "Place.Location.postal", matchPostal(left, right) ? 1 : 0,
                "Place.Location.unitNumber", scoreUnit(left, right),
                "Place.Location.street", matchField(PlaceKey.Location.street, left, right) ? 1 : 0,
                "Place.Location.address", matchField(PlaceKey.Location.address, left, right) ? 1 : 0
        );
    }

    public static Optional<Pair<String, String>> match(String text) {
        java.util.regex.Matcher matcher = UNIT_SPLIT_PATTERN.matcher(text);
        if (matcher.matches()) {
            String left = StringUtils.trimToNull(matcher.group("left"));
            String right = StringUtils.trimToNull(matcher.group("right"));
            return Optional.of(Pair.of(left, right));
        }

        return Optional.empty();
    }

    private static int scoreUnit(CorpusData left, CorpusData right) {
        String leftUnit = fixUnit(PlaceKey.Location.unitNumber.getValue(left));
        String rightUnit = fixUnit(PlaceKey.Location.unitNumber.getValue(right));
        if (leftUnit != null && rightUnit != null) {
            if (leftUnit.equalsIgnoreCase(rightUnit)) {
                return 1;
            } else {
                return -1;
            }
        }
        return 0;
    }

    private static boolean matchUnit(CorpusData left, CorpusData right) {
        String leftUnit = fixUnit(PlaceKey.Location.unitNumber.getValue(left));
        if (leftUnit == null) return false;

        String rightUnit = fixUnit(PlaceKey.Location.unitNumber.getValue(right));
        if (rightUnit == null) return false;

        return leftUnit.equalsIgnoreCase(rightUnit);
    }

    private static String fixUnit(String text) {
        if (StringUtils.isBlank(text)) return null;
return null;
//        return UNIT_PATTERN.matcher(text).replaceAll("");
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

    private static String fixPostal(String postal) {
        if (postal != null && postal.length() == 5) return "0" + postal;
        return postal;
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
}
