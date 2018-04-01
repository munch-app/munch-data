package munch.data.place.graph.matcher;

import catalyst.utils.LatLngUtils;
import com.fasterxml.jackson.databind.JsonNode;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import corpus.location.LocationClient;
import corpus.utils.FieldCollector;
import munch.data.place.elastic.ElasticClient;
import munch.data.place.graph.PlaceTree;

import javax.inject.Inject;
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

    private final LocationClient locationClient;

    @Inject
    public SpatialMatcher(LocationClient locationClient) {
        this.locationClient = locationClient;
    }

    @Override
    public Map<String, Integer> match(String placeId, CorpusData left, CorpusData right) {
        // Only use this matcher if right latLng is present
        Optional<LatLngUtils.LatLng> latLng = PlaceKey.Location.latLng.getLatLng(right);
        if (!latLng.isPresent()) return Map.of();


        Optional<LatLngUtils.LatLng> leftLatLng = PlaceKey.Location.latLng.getLatLng(left);
        Optional<String> leftPostal = PlaceKey.Location.postal.get(left).map(CorpusData.Field::getValue);

        if (leftLatLng.isPresent()) {
            double distance = latLng.get().distance(leftLatLng.get());
            if (distance <= MAX_DISTANCE) return Map.of("Place.Location.latLng", (int) distance);
            return Map.of();
        }

        if (leftPostal.isPresent()) {
            LocationClient.Data data = locationClient.geocodePostcode(leftPostal.get());
            if (data == null) return Map.of();
            double distance = latLng.get().distance(data.getLat(), data.getLng());
            if (distance <= MAX_DISTANCE) return Map.of("Place.Location.latLng", (int) distance);
            return Map.of();
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
