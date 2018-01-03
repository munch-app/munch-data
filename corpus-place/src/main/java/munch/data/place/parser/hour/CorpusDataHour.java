package munch.data.place.parser.hour;

import corpus.data.CorpusData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 22/12/2017
 * Time: 8:35 PM
 * Project: munch-data
 */
@Singleton
public final class CorpusDataHour {
    private static final Logger logger = LoggerFactory.getLogger(CorpusDataHour.class);

    private final String corpusName;
    private final String corpusKey;

    private final Map<String, DayOpenClose> days = new HashMap<>();

    public CorpusDataHour(String corpusName, String corpusKey, List<CorpusData.Field> fields) {
        this.corpusName = corpusName;
        this.corpusKey = corpusKey;

        days.put("mon", new DayOpenClose());
        days.put("tue", new DayOpenClose());
        days.put("wed", new DayOpenClose());
        days.put("thu", new DayOpenClose());
        days.put("fri", new DayOpenClose());
        days.put("sat", new DayOpenClose());
        days.put("sun", new DayOpenClose());
        fields.forEach(this::putFields);
    }

    public String getCorpusName() {
        return corpusName;
    }

    public String getCorpusKey() {
        return corpusKey;
    }

    public Map<String, DayOpenClose> getDays() {
        return days;
    }

    private void putFields(CorpusData.Field field) {
        String day = parseDay(field.getKey());
        if (day == null) return;

        String[] range = field.getValue().split("-");
        String open = range[0];
        String close = range[1];

        if (pastMidnight(open, close)) {
            days.get(day).put(open, "24:00");
            days.get(getNextDay(day)).put("00:00", close);
        } else {
            days.get(day).put(open, close);
        }
    }

    @Nullable
    private static String parseDay(String key) {
        switch (key) {
            case "Place.Hour.mon":
                return "mon";
            case "Place.Hour.tue":
                return "tue";
            case "Place.Hour.wed":
                return "wed";
            case "Place.Hour.thu":
                return "thu";
            case "Place.Hour.fri":
                return "fri";
            case "Place.Hour.sat":
                return "sat";
            case "Place.Hour.sun":
                return "sun";
            case "Place.Hour.ph":
                return "ph";
            case "Place.Hour.evePh":
                return "evePh";
            default:
                logger.warn("Unable to parse day: {}", key);
            case "Place.Hour.raw":
                return null;
        }
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
