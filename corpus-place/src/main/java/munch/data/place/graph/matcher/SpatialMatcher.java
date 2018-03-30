package munch.data.place.graph.matcher;

import catalyst.utils.LatLngUtils;
import com.fasterxml.jackson.databind.JsonNode;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import corpus.utils.FieldCollector;
import munch.data.place.elastic.ElasticClient;
import munch.data.place.graph.PlaceTree;

import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 30/3/2018
 * Time: 10:53 PM
 * Project: munch-data
 */
@Singleton
public final class SpatialMatcher implements Matcher, Searcher {
    public static final double MAX_DISTANCE = 200.0; // Metres

    @Override
    public Map<String, Integer> match(String placeId, CorpusData left, CorpusData right) {
        Optional<LatLngUtils.LatLng> latLng = PlaceKey.Location.latLng.getLatLng(left);
        if (!latLng.isPresent()) return Map.of();

        for (CorpusData.Field rightField : PlaceKey.Location.latLng.getAll(right)) {
            LatLngUtils.LatLng rightLatLng = LatLngUtils.parse(rightField.getValue());
            double distance = rightLatLng.distance(latLng.get());
            if (distance <= MAX_DISTANCE) {
                return Map.of("Place.Location.latLng", (int) distance);
            }
        }
        return Map.of();
    }

    @Override
    public Set<String> requiredFields() {
        return Set.of("Place.Location.latLng");
    }

    @Override
    public List<CorpusData> search(ElasticClient elasticClient, PlaceTree placeTree) {
        FieldCollector latLngCollector = placeTree.getFieldCollector(PlaceKey.Location.latLng);
        String latLng = latLngCollector.collectMax();
        if (latLng == null) return List.of();

        JsonNode filter = ElasticClient.filterDistance("Place.Location.latLng", latLng, MAX_DISTANCE);
        return elasticClient.search(placeTree, filter);

    }
}
