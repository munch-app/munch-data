package munch.search.place;

import munch.data.Place;
import munch.data.SearchQuery;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by: Fuxing
 * Date: 4/9/2017
 * Time: 1:02 AM
 * Project: munch-core
 */
public final class HourFilter {
    private static final Logger logger = LoggerFactory.getLogger(HourFilter.class);

    public static void filter(SearchQuery query, List<Object> places) {
        if (query.getFilter() == null) return;
        if (query.getFilter().getHour() == null) return;
        filter(query.getFilter().getHour(), places);
    }

    private static void filter(SearchQuery.Filter.Hour hourFilter, List<Object> places) {
        if (StringUtils.isAnyBlank(hourFilter.getDay(), hourFilter.getTime())) return;

        places.removeIf(object -> {
            if (!(object instanceof Place)) return false;

            Place place = (Place) object;
            if (place.getHours() == null) return true;
            if (place.getHours().isEmpty()) return true;

            int time = parseTime(hourFilter.getTime());
            for (Place.Hour hour : place.getHours()) {
                if (hour.getDay().equalsIgnoreCase(hourFilter.getDay())) {
                    if (filterHour(time, hour)) return false;
                }
            }
            return true;
        });
    }

    /**
     * @param time time
     * @param hour hour
     * @return true is is allowed
     */
    private static boolean filterHour(int time, Place.Hour hour) {
        int open = parseTime(hour.getOpen());
        int close = parseTime(hour.getClose());
        return open <= time && time <= close;
    }

    private static int parseTime(String time) {
        try {
            return Integer.parseInt(time.replace(":", ""));
        } catch (NumberFormatException e) {
            logger.error("NumberFormatException on time", e);
            return 1200;
        }
    }
}
