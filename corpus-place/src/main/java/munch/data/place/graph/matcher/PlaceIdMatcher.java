package munch.data.place.graph.matcher;

import corpus.data.CorpusData;
import corpus.field.FieldUtils;

import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 8:15 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceIdMatcher implements Matcher {

    @Override
    public Map<String, Integer> match(String placeId, CorpusData left, CorpusData right) {
        String rightPlaceId = FieldUtils.getValue(right, "Place.id");
        if (rightPlaceId == null) return Map.of();
        if (!placeId.equals(rightPlaceId)) return Map.of();

        // Whether to track Place.id that are wrong?
        return Map.of("Place.id", 1);
    }

    @Override
    public Set<String> requiredFields() {
        return Set.of("Place.id");
    }
}
