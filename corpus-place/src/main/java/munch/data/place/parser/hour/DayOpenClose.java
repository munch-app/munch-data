package munch.data.place.parser.hour;

import munch.data.structure.Place;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 22/12/2017
 * Time: 8:05 PM
 * Project: munch-data
 */
public class DayOpenClose {

    // Every 1 minutes whether its open or close
    private boolean[] minutes = new boolean[24 * 60];

    public void put(String openClose) {
        if (StringUtils.isBlank(openClose)) return;
        String[] range = openClose.split("-");
        put(range[0], range[1]);
    }

    public void put(String open, String close) {
        if (StringUtils.isAnyBlank(open, close)) return;
        int openTime = parse(open);
        int closeTime = parse(close);
        for (int i = openTime; i < minutes.length && i < closeTime; i++) {
            minutes[i] = true;
        }
    }

    public List<Place.Hour> getPlaceHours(String day) {
        List<Place.Hour> hours = new ArrayList<>();
        boolean open = false; // State
        Place.Hour hour = null;

        for (int i = 0; i < minutes.length; i++) {
            if (minutes[i] != open) {
                open = !open;

                if (open) {
                    hour = new Place.Hour();
                    hour.setDay(day);
                    hour.setOpen(toTime(i));
                } else {
                    assert hour != null;
                    hour.setClose(toTime(i));
                    hours.add(hour);
                    hour = null;
                }
            }
        }

        if (hour != null) {
            hour.setClose("23:59");
            hours.add(hour);
        }

        return hours;
    }

    static int parse(String time) {
        if (StringUtils.isBlank(time)) return -1;
        String[] split = time.split(":");
        try {
            int hour = Integer.parseInt(split[0]) * 60;
            int minutes = Integer.parseInt(split[1]);
            return hour + minutes;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static String toTime(int i) {
        int hour = i / 60;
        int min = i % 60;

        String time = "";
        if (hour < 10) time += "0";
        time += hour + ":";
        if (min < 10) time += "0";
        time += min;
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DayOpenClose that = (DayOpenClose) o;
        return Arrays.equals(minutes, that.minutes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(minutes);
    }
}
