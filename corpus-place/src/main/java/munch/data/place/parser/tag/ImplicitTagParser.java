package munch.data.place.parser.tag;

import catalyst.utils.LatLngUtils;
import corpus.data.CorpusData;
import munch.data.place.parser.hour.DayOpenClose;
import munch.data.place.parser.location.LocationDatabase;
import munch.data.structure.Place;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
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

    public List<String> parse(Place place, Collection<String> explicitTags, List<CorpusData> list) {
        List<String> tags = new ArrayList<>();

        // Parse all information to add
        tags.addAll(parseLocation(place));
        tags.addAll(parseHour(place));
        tags.addAll(explicitTags);

        return tags.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    private Set<String> parseLocation(Place place) {
        LatLngUtils.LatLng latLng = LatLngUtils.parse(place.getLocation().getLatLng());
        return locationDatabase.findTags(latLng.getLat(), latLng.getLng());
    }

    private Set<String> parseHour(Place place) {
        if (place.getHours().isEmpty()) return Set.of();

        Set<String> tags = new HashSet<>();
        if (intersect(place.getHours(), "07:45", "9:45", 4)) tags.add("breakfast");
        if (intersect(place.getHours(), "11:30", "12:30", 4)) tags.add("lunch");
        if (intersect(place.getHours(), "18:30", "20:00", 4)) tags.add("dinner");
        return tags;
    }

    /**
     * @param hours    hours to check intersect on
     * @param open     open intersect range
     * @param close    close intersect range
     * @param minCount min count of intersect by distinct on day
     * @return if intersected
     */
    private boolean intersect(List<Place.Hour> hours, String open, String close, int minCount) {
        int openTime = DayOpenClose.serializeTime(open);
        int closeTime = DayOpenClose.serializeTime(close);
        return hours.stream()
                .filter(hour -> {
                    // (StartA <= EndB) and (EndA >= StartB)
                    int placeOpen = DayOpenClose.serializeTime(hour.getOpen());
                    int placeClose = DayOpenClose.serializeTime(hour.getClose());
                    return openTime <= placeClose && closeTime >= placeOpen;
                })
                .map(Place.Hour::getDay)
                .distinct()
                .count() >= minCount;
    }
}
