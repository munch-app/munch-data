package munch.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.DayOfWeek;
import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 31/5/18
 * Time: 9:50 PM
 * Project: munch-data
 */
public final class Hour {
    private Day day;

    private String open;
    private String close;

    /**
     * @return day
     */
    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    /**
     * @return opening
     */
    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
        validate(open, close);
    }

    /**
     * @return closing
     */
    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
        validate(open, close);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hour hour = (Hour) o;
        return day == hour.day &&
                Objects.equals(open, hour.open) &&
                Objects.equals(close, hour.close);
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

    /**
     * Day in Hour
     */
    public enum Day {
        @JsonProperty("mon") mon,
        @JsonProperty("tue") tue,
        @JsonProperty("wed") wed,
        @JsonProperty("thu") thu,
        @JsonProperty("fri") fri,
        @JsonProperty("sat") sat,
        @JsonProperty("sun") sun,;

        /**
         * @param dayOfWeek input
         * @return Day output
         */
        public static Day parse(DayOfWeek dayOfWeek) {
            switch (dayOfWeek) {
                case MONDAY:
                    return mon;
                case TUESDAY:
                    return tue;
                case WEDNESDAY:
                    return wed;
                case THURSDAY:
                    return thu;
                case FRIDAY:
                    return fri;
                case SATURDAY:
                    return sat;
                case SUNDAY:
                    return sun;

                default:
                    // Never happens
                    throw new IllegalStateException("DayOfWeek not found");
            }
        }
    }

    /**
     * Format:          HH:mm
     * Time Example:    00:15
     *
     * @param open  time
     * @param close time
     * @throws IllegalArgumentException open close validation failed
     */
    public static void validate(String open, String close) throws IllegalArgumentException {
        if (open == null || close == null) return;
        if (open.length() != 5 || close.length() != 5) throw new IllegalArgumentException("open/close length != 5");
        if (close.compareTo(open) < 0) throw new IllegalArgumentException("close must be open < close");
    }
}
