package munch.data.hour.tokens;

import munch.data.hour.OpenHour;
import munch.data.utils.PatternTexts;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by: Fuxing
 * Date: 14/2/18
 * Time: 11:50 AM
 * Project: munch-data
 */
public class TimeRangeToken {
    static final DateTimeFormatter hourFormat = DateTimeFormatter.ofPattern("HH:mm");

    private final LocalTime open;
    private final LocalTime close;

    TimeRangeToken(LocalTime open, LocalTime close) {
        this.open = open;
        this.close = close;
    }

    /**
     * @param day day of time range
     * @return hour field
     */
    public OpenHour toFields(DayToken day) {
        OpenHour hour = new OpenHour();
        hour.setOpen(open.format(hourFormat));
        hour.setClose(close.format(hourFormat));
        hour.setDay(day.toDay());
        return hour;
    }

    public static void parse(PatternTexts texts) {
        // Map all Time + Range + Time to TimeRange
        texts.replace(TimeToken.class, RangeToken.class, TimeToken.class, triple -> {
            TimeToken left = (TimeToken) triple.getLeft();
            TimeToken right = (TimeToken) triple.getRight();
            return new TimeRangeToken(left.time, right.time);
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeRangeToken timeRange = (TimeRangeToken) o;
        if (!open.equals(timeRange.open)) return false;
        return close.equals(timeRange.close);
    }

    @Override
    public int hashCode() {
        int result = open.hashCode();
        result = 31 * result + close.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "(TimeRange:" + open.format(hourFormat) + "-" + close.format(hourFormat) + ")";
    }
}
