package munch.data.hour;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 14/2/18
 * Time: 9:19 AM
 * Project: munch-data
 */
public final class OpenHour {
    public enum Day {
        @JsonProperty("mon") Mon,
        @JsonProperty("tue") Tue,
        @JsonProperty("wed") Wed,
        @JsonProperty("thu") Thu,
        @JsonProperty("fri") Fri,
        @JsonProperty("sat") Sat,
        @JsonProperty("sun") Sun,
        @JsonProperty("ph") Ph,
        @JsonProperty("evePh") EvePh
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
    private String open;
    private String close;

    /**
     * @return day in enum will be string in json
     * @see Day
     */
    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    /**
     * @return opening hours
     */
    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    /**
     * @return closing hours
     */
    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
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
