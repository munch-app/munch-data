package munch.data.resolver.tag;

import munch.data.client.TagClient;
import munch.data.place.Place;
import munch.data.tag.Tag;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 1/10/18
 * Time: 3:26 PM
 * Project: munch-data
 */
@Singleton
public final class TagMapper {

    private List<Tag> defaults = new ArrayList<>();
    private Map<String, Set<Tag>> tagsMap = new HashMap<>();

    @Inject
    public TagMapper(TagClient tagClient) {
        tagClient.iterator().forEachRemaining(tag -> {
            if (tag.getName().equalsIgnoreCase("restaurant")) defaults.add(tag);

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

    public Set<Tag> get(String name) {
        return tagsMap.getOrDefault(name, Set.of());
    }

    public List<Tag> getDefaults() {
        return defaults;
    }

    public List<Place.Tag> mapDistinct(Collection<Tag> tags) {
        return tags.stream()
                .map(TagMapper::parse)
                .distinct()
                .collect(Collectors.toList());
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
