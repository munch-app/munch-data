package munch.data.hour;

import java.util.*;

/**
 * Created by: Fuxing
 * Date: 13/5/18
 * Time: 4:44 PM
 * Project: munch-data
 */
public class HourNormaliser {
    private static final Set<String> SUPPORTED_DAYS = Set.of(
            "mon", "tue", "wed", "thu", "fri", "sat", "sun"
    );

    public List<OpenHour> normalise(List<OpenHour> hours) {
        Map<String, DayOpenClose> days = new HashMap<>();
        days.put("mon", new DayOpenClose());
        days.put("tue", new DayOpenClose());
        days.put("wed", new DayOpenClose());
        days.put("thu", new DayOpenClose());
        days.put("fri", new DayOpenClose());
        days.put("sat", new DayOpenClose());
        days.put("sun", new DayOpenClose());

        List<OpenHour> hourList = new ArrayList<>();
        for (OpenHour hour : hours) {
            String day = hour.getDay().name().toLowerCase();
            if (!SUPPORTED_DAYS.contains(day)) continue;

            String open = hour.getOpen();
            String close = hour.getClose();

            if (pastMidnight(open, close)) {
                days.get(day).put(open, "24:00");
                days.get(getNextDay(day)).put("00:00", close);
            } else {
                days.get(day).put(open, close);
            }
        }
        return hourList;
    }

    private static String getNextDay(String day) {
        switch (day) {
            case "mon":
                return "tue";
            case "tue":
                return "wed";
            case "wed":
                return "thu";
            case "thu":
                return "fri";
            case "fri":
                return "sat";
            case "sat":
                return "sun";
            case "sun":
                return "mon";
            default:
                throw new RuntimeException("Unable to get next day, day is " + day);
        }
    }

    private static boolean pastMidnight(String open, String close) {
        return DayOpenClose.serializeTime(open) > DayOpenClose.serializeTime(close);
    }
}
