package munch.data.hour;

import munch.data.hour.tokens.*;
import munch.data.utils.PatternTexts;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by: Fuxing
 * Date: 21/5/2017
 * Time: 6:02 AM
 * Project: article-corpus
 */
public class HourExtractor {

    /**
     * @param texts texts with possible opening hours inside
     * @return List of Opening hours extracted, list if hours is sorted by strength, the strongest list will remain
     */
    public List<OpenHour> extract(List<String> texts) {
        return texts.stream()
                .map(this::extract)
                .filter(s -> s.size() > 1)
                .map(hours -> Pair.of(strength(hours), hours))
                .sorted((o1, o2) -> o2.getLeft().compareTo(o1.getLeft()))
                .findFirst()
                .map(Pair::getRight)
                .orElse(List.of());
    }

    /**
     * @param hourList hour list
     * @return strength of that open list
     */
    private long strength(List<OpenHour> hourList) {
        return hourList.stream()
                .mapToLong(OpenHour::getMinutes)
                .sum();
    }

    /**
     * @param text text with opening hours inside
     * @return List of Opening hours extracted
     */
    public List<OpenHour> extract(String text) {
        if (StringUtils.isBlank(text)) return List.of();

        // Create texts and parse all: must be in order
        PatternTexts texts = parse(text);

        // Collect all days and times
        List<DaysToken> days = texts.collect(DaysToken.class);
        List<TimeRangesToken> times = texts.collect(TimeRangesToken.class);
        if (days.isEmpty() && times.isEmpty()) return Collections.emptyList();

        // Check if there is only single time range, if so form up it as daily
        if (times.size() == 1 && days.size() == 1) {
            return times.get(0).set.stream()
                    .flatMap(range -> days.get(0).set.stream().map(range::toFields))
                    .collect(Collectors.toList());
        }

        if (times.size() == 1 && days.isEmpty()) {
            return times.get(0).set.stream()
                    .flatMap(range -> Arrays.stream(DayToken.daily()).map(range::toFields))
                    .collect(Collectors.toList());
        }
        return parseFields(texts);
    }

    PatternTexts parse(String text) {
        // Create texts and parse all: must be in order
        PatternTexts texts = new PatternTexts(text);
        // Filter Tokens
        PriceToken.parse(texts);

        // Symbol Tokens
        SeperatorToken.parse(texts);
        AndToken.parse(texts);
        RangeToken.parse(texts);
        DayToken.parse(texts);

        // Time Based Tokens
        TimeToken.parse(texts);
        ClosedToken.parse(texts);
        RemoveToken.parse(texts);
        LastOrderToken.parse(texts);

        // Joining Tokens
        DaysToken.parse(texts);
        TimeRangeToken.parse(texts);
        TimeRangesToken.parse(texts);
        texts.removeIf(o -> o instanceof AndToken);
        return texts;
    }

    List<OpenHour> parseFields(PatternTexts texts) {
        List<OpenHour> fields = new ArrayList<>();
        ListIterator<Object> iterator = texts.listIterator();
        while (iterator.hasNext()) {
            Object days = iterator.next();
            if (!iterator.hasNext()) break;
            Object time = iterator.next();

            if (days instanceof DaysToken && time instanceof TimeRangesToken) {
                for (DayToken day : ((DaysToken) days).set) {
                    for (TimeRangeToken timeRange : ((TimeRangesToken) time).set) {
                        fields.add(timeRange.toFields(day));
                    }
                }
            } else {
                iterator.previous();
            }
        }
        return fields;
    }
}
