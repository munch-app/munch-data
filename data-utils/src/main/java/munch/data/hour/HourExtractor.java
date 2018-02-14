package munch.data.hour;

import munch.data.hour.tokens.*;
import munch.data.utils.PatternTexts;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by: Fuxing
 * Date: 21/5/2017
 * Time: 6:02 AM
 * Project: article-corpus
 */
public class HourExtractor {

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
        SeperatorToken.parse(texts);
        AndToken.parse(texts);
        RangeToken.parse(texts);
        DayToken.parse(texts);

        TimeToken.parse(texts);
        ClosedToken.parse(texts);
        RemoveToken.parse(texts);
        LastOrderToken.parse(texts);

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
