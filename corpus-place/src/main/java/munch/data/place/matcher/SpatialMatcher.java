package munch.data.place.matcher;

import catalyst.utils.LatLngUtils;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;

import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

/**
 * Created by: Fuxing
 * Date: 5/12/2017
 * Time: 11:14 PM
 * Project: munch-data
 */
@Singleton
public final class SpatialMatcher {
    public static final double MAX_DISTANCE = 200.0; // Metres

    /**
     * @param insides data already inside
     * @param outside data outside coming in
     * @return true is outside data belongs with inside
     */
    public boolean match(List<CorpusData> insides, CorpusData outside) {
        return matchLatLng(insides, outside);
    }

    public boolean matchLatLng(List<CorpusData> insides, CorpusData outside) {
        Optional<LatLngUtils.LatLng> outsideLatLng = PlaceKey.Location.latLng.getLatLng(outside);
        if (!outsideLatLng.isPresent()) return false;

        LatLngUtils.LatLng rightLatLng = outsideLatLng.get();
        for (CorpusData data : insides) {
            for (CorpusData.Field field : PlaceKey.Location.latLng.getAll(data)) {
                if (matchDistance(field.getValue(), rightLatLng)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean matchDistance(String left, LatLngUtils.LatLng rightLatLng) {
        LatLngUtils.LatLng leftLatLng = LatLngUtils.parse(left);
        return leftLatLng.distance(rightLatLng) <= MAX_DISTANCE;
    }
}
