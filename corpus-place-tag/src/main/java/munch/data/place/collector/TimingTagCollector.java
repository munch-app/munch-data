package munch.data.place.collector;

import corpus.data.CorpusData;
import munch.data.structure.Place;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 8/3/18
 * Time: 10:00 PM
 * Project: munch-data
 */
public class TimingTagCollector {
    // TODO

    public List<String> parse(Place place, Collection<String> explicitTags, List<CorpusData> list) {
        List<String> tags = new ArrayList<>();

        // Parse all information to add
        tags.addAll(parseHour(place));
        tags.addAll(explicitTags);

        return tags.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    private Set<String> parseHour(Place place) {
        if (place.getHours().isEmpty()) return Set.of();

        Set<String> tags = new HashSet<>();
        if (intersect(place.getHours(), "07:45", "09:45", 4)) tags.add("breakfast");
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
        return false;
//        int openTime = DayOpenClose.serializeTime(open);
//        int closeTime = DayOpenClose.serializeTime(close);
//        return hours.stream()
//                .filter(hour -> {
//                    // (StartA <= EndB) and (EndA >= StartB)
//                    int placeOpen = DayOpenClose.serializeTime(hour.getOpen());
//                    int placeClose = DayOpenClose.serializeTime(hour.getClose());
//                    return openTime <= placeClose && closeTime >= placeOpen;
//                })
//                .map(Place.Hour::getDay)
//                .distinct()
//                .count() >= minCount;
    }
}
