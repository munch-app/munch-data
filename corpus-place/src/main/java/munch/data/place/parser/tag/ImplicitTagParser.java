package munch.data.place.parser.tag;

import catalyst.utils.LatLngUtils;
import corpus.data.CorpusData;
import munch.data.place.parser.location.LocationDatabase;
import munch.data.structure.Place;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 6/2/18
 * Time: 2:36 PM
 * Project: munch-data
 */
@Singleton
public class ImplicitTagParser {
    private final LocationDatabase locationDatabase;

    @Inject
    public ImplicitTagParser(LocationDatabase locationDatabase) {
        this.locationDatabase = locationDatabase;
    }

    public List<String> parse(Place place, List<CorpusData> list) {
        List<String> tags = new ArrayList<>();

        // Parse all information to add
        tags.addAll(parseLocation(place));

        return tags.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    private Set<String> parseLocation(Place place) {
        LatLngUtils.LatLng latLng = LatLngUtils.parse(place.getLocation().getLatLng());
        return locationDatabase.findTags(latLng.getLat(), latLng.getLng());
    }
}
