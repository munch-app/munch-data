package munch.data.resolver.tag;

import catalyst.edit.HourEdit;
import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import munch.data.tag.Tag;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 1/10/18
 * Time: 3:23 PM
 * Project: munch-data
 */
@Singleton
public final class TagTimeResolver {
    private static final Logger logger = LoggerFactory.getLogger(TagTimeResolver.class);

    private final TagMapper mapper;

    @Inject
    public TagTimeResolver(TagMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * @param mutation with opening hours
     * @return Set of Timing tag if hours is available
     */
    public Set<Tag> resolve(PlaceMutation mutation) {
        List<MutationField<List<HourEdit>>> hours = mutation.getHour();
        if (hours.isEmpty()) return Set.of();
        List<HourEdit> hourEdits = hours.get(0).getValue();
        if (hourEdits.isEmpty()) {
            logger.warn("Required PlaceMutation.hour[0] is empty.");
            return Set.of();
        }

        Set<Tag> tags = new HashSet<>();
        if (isTimeIntersect(hourEdits, "07:45", "09:45", 4)) tags.addAll(mapper.get("breakfast"));
        if (isTimeIntersect(hourEdits, "11:30", "12:30", 4)) tags.addAll(mapper.get("lunch"));
        if (isTimeIntersect(hourEdits, "18:30", "20:00", 4)) tags.addAll(mapper.get("dinner"));
        return tags;
    }

    /**
     * @param hours    hours to check intersect on
     * @param open     open intersect range
     * @param close    close intersect range
     * @param minCount min count of intersect by distinct on day
     * @return if intersected
     */
    private boolean isTimeIntersect(List<HourEdit> hours, String open, String close, int minCount) {
        int openTime = serializeTime(open);
        int closeTime = serializeTime(close);
        return hours.stream()
                .filter(hour -> {
                    // (StartA <= EndB) and (EndA >= StartB)
                    int placeOpen = serializeTime(hour.getOpen());
                    int placeClose = serializeTime(hour.getClose());
                    return openTime <= placeClose && closeTime >= placeOpen;
                })
                .map(HourEdit::getDay)
                .distinct()
                .count() >= minCount;
    }

    public static int serializeTime(String time) {
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

}
