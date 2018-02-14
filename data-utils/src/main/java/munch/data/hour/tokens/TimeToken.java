package munch.data.hour.tokens;

import munch.data.utils.PatternSplit;
import munch.data.utils.PatternTexts;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.time.LocalTime;
import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 14/2/18
 * Time: 11:50 AM
 * Project: munch-data
 */
public class TimeToken {
    static final PatternSplit PatternNoon = PatternSplit.compile("\\b(afternoon|12 ?noon|noon|12[:.]00|12 ?pm)\\b", Pattern.CASE_INSENSITIVE);
    static final PatternSplit PatternMidnight = PatternSplit.compile("\\b(midnight|24[:.]00|12 ?am)\\b", Pattern.CASE_INSENSITIVE);

    static final PatternSplit PatternAmPm = PatternSplit.compile("\\b(?<hour>0?[0-9]|1[0-2])([:.]?(?<min>[0-5][0-9]))? ?(?<period>am|pm)\\b", Pattern.CASE_INSENSITIVE);
    static final PatternSplit Pattern24 = PatternSplit.compile("\\b(?<hour>0?[0-9]|1[0-9]|2[0-3])([:.]?(?<min>[0-5][0-9]))?\\b", Pattern.CASE_INSENSITIVE);

    public final LocalTime time;

    TimeToken(LocalTime time) {
        this.time = time;
    }

    public static void parse(PatternTexts texts) {
        texts.replace(PatternNoon, new TimeToken(LocalTime.NOON));
        texts.replace(PatternMidnight, new TimeToken(LocalTime.MIDNIGHT));

        texts.replace(PatternAmPm, matcher -> {
            String hour = matcher.group("hour");
            String min = matcher.group("min");
            String period = matcher.group("period");
            return parse(hour, min, period);
        });

        texts.replace(Pattern24, matcher -> {
            String hour = matcher.group("hour");
            String min = matcher.group("min");
            return parse(hour, min, null);
        });
    }

    static TimeToken parse(String hourS, String minS, @Nullable String period) {
        int hour = Integer.parseInt(hourS);
        hour += hour != 12 && StringUtils.equalsIgnoreCase(period, "pm") ? 12 : 0;
        int min = minS == null ? 0 : Integer.parseInt(minS);
        return new TimeToken(LocalTime.of(hour, min));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeToken time1 = (TimeToken) o;

        return time.equals(time1.time);
    }

    @Override
    public int hashCode() {
        return time.hashCode();
    }

    @Override
    public String toString() {
        return "(Time:" + time.format(TimeRangeToken.hourFormat) + ")";
    }
}
