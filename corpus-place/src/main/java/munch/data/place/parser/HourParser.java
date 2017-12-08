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
import java.util.stream.Stream;

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

        if (values.isEmpty()) return Collections.emptyList();

        // Get the value appearance count for the collection of hours
        int max = values.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getValue)
                .orElse(1);

        // Filter out the possibles set of hours
        List<Set<Place.Hour>> possibles = values.entrySet().stream()
                .filter(entry -> entry.getValue() == max)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Deterministic select of possible place hour
        return selectOne(possibles);
    }

    /**
     * Deterministic selection of List of List of Place.Hour
     *
     * @param list multiple possible set of opening hours
     * @return select one that is grantee always in same order and always be selected
     */
    private List<Place.Hour> selectOne(List<Set<Place.Hour>> list) {
        if (list.isEmpty()) return Collections.emptyList();

        list.sort(Comparator.comparingInt(Set::hashCode));
        return new ArrayList<>(list.get(0));
    }

    private Map<String, Set<Place.Hour>> collect(List<CorpusData> list) {
        Map<String, Set<Place.Hour>> map = new HashMap<>();

        for (CorpusData data : list) {
            Set<Place.Hour> hours = collect(data, PlaceKey.Hour.week)
                    .stream().flatMap(this::parseFields)
                    .collect(Collectors.toSet());
            if (!hours.isEmpty()) {
                map.put(data.getCorpusKey(), hours);
            }
        }
        return map;
    }

    private Stream<Place.Hour> parseFields(CorpusData.Field field) {
        String day = parseDay(field.getKey());
        if (day == null) return Stream.empty();

        String[] range = field.getValue().split("-");

        if (pastMidnight(range[0], range[1])) {
            Place.Hour firstDay = new Place.Hour();
            firstDay.setOpen(range[0]);
            firstDay.setClose("24:00");
            firstDay.setDay(day);

            Place.Hour secondDay = new Place.Hour();
            secondDay.setOpen("00:00");
            secondDay.setClose(range[1]);
            secondDay.setDay(getNextDay(day));
            return Stream.of(firstDay, secondDay);
        } else {
            Place.Hour hour = new Place.Hour();
            hour.setOpen(range[0]);
            hour.setClose(range[1]);
            hour.setDay(day);
            return Stream.of(hour);
        }
    }

    @Nullable
    private static String parseDay(String key) {
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

    private static String getNextDay(String day) {
        switch (day) {
            case "mon":
                return "tue";
            case "tue":
                return "wed";
            case "wed":
                return "thu";
            case "thu":
                return "fri";
            case "fri":
                return "sat";
            case "sat":
                return "sun";
            case "sun":
                return "mon";
            default:
                throw new RuntimeException("Unable to get next day, day is " + day);
        }
    }

    private static boolean pastMidnight(String open, String close) {
        int openInt = Integer.parseInt(open.replace(":", ""));
        int closeInt = Integer.parseInt(close.replace(":", ""));
        return openInt > closeInt;
    }
}
