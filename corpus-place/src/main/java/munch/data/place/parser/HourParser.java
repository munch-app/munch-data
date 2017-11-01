package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.structure.Place;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 10:13 PM
 * Project: munch-data
 */
@Singleton
public final class HourParser extends AbstractParser<List<Place.Hour>> {
    private static final Logger logger = LoggerFactory.getLogger(HourParser.class);

    @Override
    public List<Place.Hour> parse(Place place, List<CorpusData> list) {
        Map<String, Set<Place.Hour>> map = collect(list);
        if (map.isEmpty()) return Collections.emptyList();

        // Map them in set with count to duplicated sets
        Map<Set<Place.Hour>, Integer> values = new HashMap<>();
        for (Set<Place.Hour> hours : map.values()) {
            values.compute(hours, (h, i) -> i == null ? 1 : i + 1);
        }

        return values.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .map(ArrayList::new)
                .orElseGet(ArrayList::new);
    }

    private Map<String, Set<Place.Hour>> collect(List<CorpusData> list) {
        Map<String, Set<Place.Hour>> map = new HashMap<>();

        for (CorpusData data : list) {
            Set<Place.Hour> hours = collect(data, PlaceKey.Hour.week)
                    .stream().map(this::parseHour)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!hours.isEmpty()) {
                map.put(data.getCorpusKey(), hours);
            }
        }
        return map;
    }

    @Nullable
    private Place.Hour parseHour(CorpusData.Field field) {
        String day = parseDay(field.getKey());
        if (day == null) return null;

        String[] range = field.getValue().split("-");
        Place.Hour hour = new Place.Hour();
        hour.setOpen(range[0]);
        hour.setClose(range[1]);
        hour.setDay(day);
        return hour;
    }

    @Nullable
    public String parseDay(String key) {
        switch (key) {
            case "Place.Hour.mon":
                return "mon";
            case "Place.Hour.tue":
                return "tue";
            case "Place.Hour.wed":
                return "wed";
            case "Place.Hour.thu":
                return "thu";
            case "Place.Hour.fri":
                return "fri";
            case "Place.Hour.sat":
                return "sat";
            case "Place.Hour.sun":
                return "sun";
            case "Place.Hour.ph":
                return "ph";
            case "Place.Hour.evePh":
                return "evePh";
            default:
                logger.warn("Unable to parse day: {}", key);
            case "Place.Hour.raw":
                return null;
        }
    }
}
