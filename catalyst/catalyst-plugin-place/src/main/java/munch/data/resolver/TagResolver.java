package munch.data.resolver;

import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import munch.data.client.TagClient;
import munch.data.place.Place;
import munch.data.tag.Tag;

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
    private Place.Tag restaurant = new Place.Tag();
    private Map<String, Set<Tag>> tagsMap = new HashMap<>();

    @Inject
    public TagResolver(TagClient tagClient) {
        tagClient.iterator().forEachRemaining(tag -> {
            if (tag.getName().equalsIgnoreCase("restaurant")) {
                restaurant = parse(tag);
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
        return clean(tags);
    }

    public List<Place.Tag> clean(List<String> tags) {
        Set<Tag> collected = tags.stream()
                .map(s -> tagsMap.get(s.toLowerCase()))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        List<Place.Tag> finalList = new ArrayList<>();
        select(1, 1, collected, finalList);
        select(2, 1, collected, finalList);
        select(3, 2, collected, finalList);
        collected.forEach(tag -> finalList.add(parse(tag)));

        // if not tag found, restaurant will be returned
        if (finalList.isEmpty()) return List.of(restaurant);
        return finalList;
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
