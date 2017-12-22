package munch.data.place.parser.hour;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.place.parser.AbstractParser;
import munch.data.structure.Place;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.*;
import java.util.function.Function;
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
    private static final Comparator<Map.Entry<DayOpenClose, Long>> HOUR_COMPARATOR = comparator();

    @Override
    public List<Place.Hour> parse(Place place, List<CorpusData> list) {
        // Collect them into corpus: hours
        List<CorpusDataHour> hourList = collect(list);
        for (String corpusName : priorityCorpus) {
            for (CorpusDataHour corpusDataHour : hourList) {
                if (corpusDataHour.getCorpusName().equals(corpusName)) {
                    // If contain priority corpus data, use that only
                    hourList = List.of(corpusDataHour);
                    break;
                }
            }
        }

        if (hourList.isEmpty()) return Collections.emptyList();

        // Map them in set with count to duplicated sets
        List<Place.Hour> hours = new ArrayList<>();
        hours.addAll(collectDayMax(hourList, "mon"));
        hours.addAll(collectDayMax(hourList, "tue"));
        hours.addAll(collectDayMax(hourList, "wed"));
        hours.addAll(collectDayMax(hourList, "thu"));
        hours.addAll(collectDayMax(hourList, "fri"));
        hours.addAll(collectDayMax(hourList, "sat"));
        hours.addAll(collectDayMax(hourList, "sun"));

        // Deterministic sort of place hour
        return hours.stream()
                .sorted(Comparator.comparing(Place.Hour::getDay)
                        .thenComparing(Place.Hour::getOpen)
                        .thenComparing(Place.Hour::getClose))
                .collect(Collectors.toList());
    }

    private List<Place.Hour> collectDayMax(List<CorpusDataHour> corpusHours, String day) {
        return corpusHours.stream()
                .map(corpusDataHour -> corpusDataHour.getDays().get(day))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(HOUR_COMPARATOR)
                .map(entry -> entry.getKey().getPlaceHours(day))
                .orElse(List.of());
    }

    private List<CorpusDataHour> collect(List<CorpusData> list) {
        List<CorpusDataHour> hourList = new ArrayList<>();
        for (CorpusData data : list) {
            List<CorpusData.Field> hours = collect(data, PlaceKey.Hour.week);
            if (!hours.isEmpty()) {
                hourList.add(new CorpusDataHour(data.getCorpusName(), data.getCorpusKey(), hours));
            }
        }
        return hourList;
    }

    private static Comparator<Map.Entry<DayOpenClose, Long>> comparator() {
        Comparator<Map.Entry<DayOpenClose, Long>> comparator = Comparator.comparingLong(Map.Entry::getValue);
        comparator = comparator.reversed();
        comparator = comparator.thenComparingInt(value -> value.getKey().hashCode());
        return comparator;
    }
}
