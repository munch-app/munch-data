package munch.data.hour;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 14/2/18
 * Time: 9:19 AM
 * Project: munch-data
 */
public final class OpenHour {
    static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public enum Day {
        Mon,
        Tue,
        Wed,
        Thu,
        Fri,
        Sat,
        Sun,
        Ph,
        EvePh
    }

    private Day day;

    /**
     * HH:mm
     * 00:00 - 23:59
     * Midnight - 1 Min before midnight Max
     * <p>
     * 12:00 - 22:00
     * Noon - 10pm
     * <p>
     * 08:00 - 19:00
     * 8am - 7pm
     * <p>
     * Not Allowed:
     * 20:00 - 04:00
     * 20:00 - 24:00
     * Not allowed to put 24:00 Highest is 23:59
     * Not allowed to cross to another day
     */
    private final String open;
    private final String close;
    private final long minutes;

    public OpenHour(Day day, LocalTime open, LocalTime close) {
        this.day = day;
        this.open = open.format(HOUR_FORMAT);
        this.close = close.format(HOUR_FORMAT);
        this.minutes = open.until(close, ChronoUnit.MINUTES);
    }

    /**
     * @return day in enum will be string in json
     * @see Day
     */
    public Day getDay() {
        return day;
    }


    /**
     * @return opening hours
     */
    public String getOpen() {
        return open;
    }

    /**
     * @return closing hours
     */
    public String getClose() {
        return close;
    }

    public long getMinutes() {
        return minutes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpenHour openHour = (OpenHour) o;
        return day == openHour.day &&
                Objects.equals(open, openHour.open) &&
                Objects.equals(close, openHour.close);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, open, close);
    }

    @Override
    public String toString() {
        return "Hour{" +
                "day=" + day +
                ", open='" + open + '\'' +
                ", close='" + close + '\'' +
                '}';
    }
}
