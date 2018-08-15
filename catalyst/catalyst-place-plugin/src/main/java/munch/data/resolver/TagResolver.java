package munch.data.resolver;

import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import munch.data.client.TagClient;
import munch.data.place.Place;
import munch.data.tag.Tag;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 6/6/18
 * Time: 6:15 PM
 * Project: munch-data
 */
@Singleton
public final class TagResolver {
    private Place.Tag restaurant = new Place.Tag();
    private final Supplier<Map<String, Tag>> supplier;

    @Inject
    public TagResolver(TagClient tagClient) {
        this.supplier = Suppliers.memoizeWithExpiration(() -> {
            Map<String, Tag> map = new HashMap<>();

            tagClient.iterator().forEachRemaining(tag -> {
                if (tag.getName().equalsIgnoreCase("restaurant")) {
                    restaurant = parse(tag);
                }

                // Put Names
                tag.getNames().forEach(s -> map.put(s.toLowerCase(), tag));
                // Put Remapping
                tag.getPlace().getRemapping().forEach(s -> map.put(s.toLowerCase(), tag));
            });
            return map;
        }, 8, TimeUnit.HOURS);
    }

    public List<Place.Tag> resolve(PlaceMutation mutation) {
        List<@NotNull String> tags = mutation.getTag().stream()
                .map(MutationField::getValue)
                .collect(Collectors.toList());
        return clean(tags);
    }

    public List<Place.Tag> clean(List<String> tags) {
        Map<String, Tag> map = supplier.get();
        Set<Tag> collected = tags.stream()
                .map(s -> map.get(s.toLowerCase()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Place.Tag> parsedList = new ArrayList<>();
        select(1, 1, collected, parsedList);
        select(2, 1, collected, parsedList);
        select(3, 2, collected, parsedList);
        collected.forEach(tag -> parsedList.add(parse(tag)));

        // if not tag found, restaurant will be returned
        if (parsedList.isEmpty()) return List.of(restaurant);
        return parsedList;
    }

    private static void select(int level, int count, Set<Tag> tags, List<Place.Tag> list) {
        List<Tag> collected = tags.stream()
                .filter(tag -> {
                    if (tag.getPlace().getLevel() == null) return false;
                    return tag.getPlace().getLevel() == level;
                })
                .sorted((o1, o2) -> Double.compare(o2.getPlace().getOrder(), o1.getPlace().getOrder()))
                .limit(count)
                .collect(Collectors.toList());
        tags.removeAll(collected);

        for (Tag tag : collected) {
            list.add(parse(tag));
        }
    }

    private static Place.Tag parse(Tag tag) {
        Place.Tag placeTag = new Place.Tag();
        placeTag.setTagId(tag.getTagId());
        placeTag.setName(tag.getName());
        placeTag.setType(tag.getType());
        return placeTag;
    }
}
