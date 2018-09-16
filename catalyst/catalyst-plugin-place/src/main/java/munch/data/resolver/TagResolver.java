package munch.data.resolver;

import catalyst.edit.HourEdit;
import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import munch.data.client.TagClient;
import munch.data.place.Place;
import munch.data.tag.Tag;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 6/6/18
 * Time: 6:15 PM
 * Project: munch-data
 */
@Singleton
public final class TagResolver {
    private static final Logger logger = LoggerFactory.getLogger(TagResolver.class);

    private Tag restaurant;
    private Map<String, Set<Tag>> tagsMap = new HashMap<>();

    @Inject
    public TagResolver(TagClient tagClient) {
        tagClient.iterator().forEachRemaining(tag -> {
            if (tag.getName().equalsIgnoreCase("restaurant")) {
                restaurant = tag;
            }

            // Put Names
            tag.getNames().forEach(s -> {
                tagsMap.computeIfAbsent(s.toLowerCase(), s1 -> new HashSet<>()).add(tag);
            });

            // Put Remapping
            tag.getPlace().getRemapping().forEach(s -> {
                tagsMap.computeIfAbsent(s.toLowerCase(), s1 -> new HashSet<>()).add(tag);
            });
        });
    }

    public List<Place.Tag> resolve(PlaceMutation mutation) {
        List<@NotNull String> tags = mutation.getTag().stream()
                .map(MutationField::getValue)
                .collect(Collectors.toList());

        List<Tag> finalTags = new ArrayList<>(clean(tags));

        // Add hours to Tag list if don't exist
        getTimingTags(mutation).forEach(tag -> {
            if (finalTags.contains(tag)) return;
            finalTags.add(tag);
        });

        return finalTags.stream()
                .map(TagResolver::parse)
                .collect(Collectors.toList());
    }

    public List<Tag> clean(List<String> tags) {
        Set<Tag> provider = tags.stream()
                .map(s -> tagsMap.get(s.toLowerCase()))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        List<Tag> collector = new ArrayList<>();
        select(1, 1, provider, collector);
        select(2, 1, provider, collector);
        select(3, 2, provider, collector);

        // Add remaining provider to collector
        collector.addAll(provider);

        // if not tag found, restaurant will be returned
        if (collector.isEmpty() && restaurant != null) return List.of(restaurant);
        return collector;
    }

    private static void select(int level, int count, Set<Tag> provider, List<Tag> collector) {
        List<Tag> collected = provider.stream()
                .filter(tag -> {
                    if (tag.getPlace().getLevel() == null) return false;
                    return tag.getPlace().getLevel() == level;
                })
                .sorted((o1, o2) -> Double.compare(o2.getPlace().getOrder(), o1.getPlace().getOrder()))
                .limit(count)
                .collect(Collectors.toList());

        // Remove from provider and add all collected to collector
        provider.removeAll(collected);
        collector.addAll(collected);
    }

    public Set<Tag> getTimingTags(PlaceMutation mutation) {
        List<MutationField<List<HourEdit>>> hours = mutation.getHour();
        if (hours.isEmpty()) return Set.of();
        List<HourEdit> hourEdits = hours.get(0).getValue();
        if (hourEdits.isEmpty()) {
            logger.warn("Required PlaceMutation.hour[0] is empty.");
            return Set.of();
        }

        Set<Tag> tags = new HashSet<>();
        if (isTimeIntersect(hourEdits, "07:45", "09:45", 4)) tags.addAll(tagsMap.getOrDefault("breakfast", Set.of()));
        if (isTimeIntersect(hourEdits, "11:30", "12:30", 4)) tags.addAll(tagsMap.getOrDefault("lunch", Set.of()));
        if (isTimeIntersect(hourEdits, "18:30", "20:00", 4)) tags.addAll(tagsMap.getOrDefault("dinner", Set.of()));
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

    /**
     * @param tag converted to Place.Tag
     * @return Place.Tag
     */
    private static Place.Tag parse(Tag tag) {
        Place.Tag placeTag = new Place.Tag();
        placeTag.setTagId(tag.getTagId());
        placeTag.setName(tag.getName());
        placeTag.setType(tag.getType());
        return placeTag;
    }
}
