package munch.data.place.collector;

import munch.data.structure.Place;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 8/3/18
 * Time: 10:00 PM
 * Project: munch-data
 */
public class TimingTagCollector {
    public Set<String> get(Place place) {
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
        int openTime = serializeTime(open);
        int closeTime = serializeTime(close);
        return hours.stream()
                .filter(hour -> {
                    // (StartA <= EndB) and (EndA >= StartB)
                    int placeOpen = serializeTime(hour.getOpen());
                    int placeClose = serializeTime(hour.getClose());
                    return openTime <= placeClose && closeTime >= placeOpen;
                })
                .map(Place.Hour::getDay)
                .distinct()
                .count() >= minCount;
    }

    public static int serializeTime(String time) {
        if (StringUtils.isBlank(time)) return -1;
        String[] split = time.split(":");
        try {
            int hour = Integer.parseInt(split[0]) * 60;
            int minutes = Integer.parseInt(split[1]);
            return hour + minutes;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
