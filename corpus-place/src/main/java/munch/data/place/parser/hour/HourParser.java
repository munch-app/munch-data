package munch.data.place.parser.hour;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.place.parser.AbstractParser;
import munch.data.structure.Place;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Comparator<Map.Entry<DayOpenClose, Long>> HOUR_COMPARATOR = hourComparator();
    private static final Comparator<Place.Hour> DETERMINISTIC_COMPARATOR = deterministicComparator();

    private static final List<String> RELIABLE_SOURCE = List.of("Global.Facebook.Place");

    @Override
    public List<Place.Hour> parse(Place place, List<CorpusData> list) {
        // Collect them into corpus: hours
        List<CorpusDataHour> hourList = collect(list);


        // If contain priority corpus data, use that only
        for (String corpusName : priorityCorpus) {
            for (CorpusDataHour corpusDataHour : hourList) {
                if (corpusDataHour.getCorpusName().equals(corpusName)) {
                    return convertAll(corpusDataHour);
                }
            }
        }

        // If contain reliable corpus data, use that only
        for (String corpusName : RELIABLE_SOURCE) {
            for (CorpusDataHour corpusDataHour : hourList) {
                if (corpusDataHour.getCorpusName().equals(corpusName)) {
                    return convertAll(corpusDataHour);
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
                .sorted(DETERMINISTIC_COMPARATOR)
                .collect(Collectors.toList());
    }

    /**
     * @param corpusHours list of hours
     * @param day         day to collect
     * @return List of Hour for the day
     */
    private List<Place.Hour> collectDayMax(List<CorpusDataHour> corpusHours, String day) {
        List<DayOpenClose> openCloseList = new ArrayList<>();

        // Collect all open close for that day if its open
        for (CorpusDataHour corpusHour : corpusHours) {
            DayOpenClose openClose = corpusHour.getDays().get(day);
            if (openClose.isOpen()) openCloseList.add(openClose);
        }

        if (openCloseList.isEmpty()) return List.of();

        if (openCloseList.size() == 1) {
            // If only 1 source says its open
            // Return it if its open for longer or equal to 2 hour 30 minutes
            DayOpenClose openClose = openCloseList.get(0);
            if (openClose.openMinutes() < 150) return List.of();
            return openClose.getPlaceHours(day);
        } else {
            final int required = openCloseList.size() / 2;
            DayOpenClose openClose = new DayOpenClose();

            for (int i = 0; i < DayOpenClose.TOTAL_MINUTES; i++) {
                int current = 0;
                for (DayOpenClose dayOpenClose : openCloseList) {
                    if (dayOpenClose.isOpen(i)) {
                        current++;

                        if (current >= required) {
                            // Approved
                            openClose.putMinute(i, true);
                            break;
                        }
                    }
                }
            }

            return openClose.getPlaceHours(day);
        }
    }

    /**
     * @param list corpus data
     * @return CorpusDataHour structured into corpusName & corpusKey group
     */
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

    private List<Place.Hour> convertAll(CorpusDataHour corpusDataHour) {
        List<Place.Hour> hours = new ArrayList<>();
        hours.addAll(corpusDataHour.getDays().get("mon").getPlaceHours("mon"));
        hours.addAll(corpusDataHour.getDays().get("tue").getPlaceHours("tue"));
        hours.addAll(corpusDataHour.getDays().get("wed").getPlaceHours("wed"));
        hours.addAll(corpusDataHour.getDays().get("thu").getPlaceHours("thu"));
        hours.addAll(corpusDataHour.getDays().get("fri").getPlaceHours("fri"));
        hours.addAll(corpusDataHour.getDays().get("sat").getPlaceHours("sat"));
        hours.addAll(corpusDataHour.getDays().get("sun").getPlaceHours("sun"));
        return hours;
    }

    private static Comparator<Map.Entry<DayOpenClose, Long>> hourComparator() {
        Comparator<Map.Entry<DayOpenClose, Long>> comparator = Comparator.comparingLong(Map.Entry::getValue);
        comparator = comparator.reversed();
        comparator = comparator.thenComparingInt(value -> value.getKey().hashCode());
        return comparator;
    }

    private static Comparator<Place.Hour> deterministicComparator() {
        return Comparator.comparing(Place.Hour::getDay)
                .thenComparing(Place.Hour::getOpen)
                .thenComparing(Place.Hour::getClose);
    }
}
