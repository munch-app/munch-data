package munch.data.place.graph.matcher;

import corpus.data.CorpusData;
import corpus.field.FieldUtils;

import java.util.Map;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 8:15 PM
 * Project: munch-data
 */
public final class PlaceIdMatcher implements Matcher {

    @Override
    public Map<String, Integer> match(CorpusData left, CorpusData right) {
        // TODO since its from CatalystId
        String leftPlaceId = FieldUtils.getValue(left, "Place.id");
        String rightPlaceId = FieldUtils.getValue(right, "Place.id");
        if (leftPlaceId == null) return Map.of();
        if (!leftPlaceId.equals(rightPlaceId)) return Map.of();

        return Map.of("Place.id", 1);
    }

    @Override
    public Set<String> requiredFields() {
        return Set.of("Place.id");
    }
}
