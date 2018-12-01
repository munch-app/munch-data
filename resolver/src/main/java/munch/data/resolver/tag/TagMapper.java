package munch.data.resolver.tag;

import com.google.common.io.Resources;
import munch.data.client.TagClient;
import munch.data.place.Place;
import munch.data.tag.Tag;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 1/10/18
 * Time: 3:26 PM
 * Project: munch-data
 */
@SuppressWarnings("UnstableApiUsage")
public class TagMapper {
    private Map<String, Set<Tag>> tagsMap = new HashMap<>();

    @Inject
    public TagMapper(TagClient tagClient) {
        List<String> postfixes;
        try {
            URL url = Resources.getResource("tag-postfix.txt");
            postfixes = Resources.readLines(url, Charset.defaultCharset());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        tagClient.iterator().forEachRemaining(tag -> {
            // Put Names
            tag.getNames().forEach(s -> {
                tagsMap.computeIfAbsent(s.toLowerCase(), s1 -> new HashSet<>()).add(tag);
            });

            // Put Remapping
            tag.getPlace().getRemapping().forEach(s -> {
                tagsMap.computeIfAbsent(s.toLowerCase(), s1 -> new HashSet<>()).add(tag);
            });

            // Put with Postfix
            tag.getNames().forEach(s -> postfixes.forEach(postfix -> {
                tagsMap.computeIfAbsent(s.toLowerCase() + " " + postfix.toLowerCase(), s1 -> new HashSet<>()).add(tag);
            }));

            // TODO: Known Prefix? #

            // Put with -
            tag.getNames().forEach(s -> {
                s = s.toLowerCase().replaceAll(" ", "-");
                tagsMap.computeIfAbsent(s, s1 -> new HashSet<>()).add(tag);
            });

            // Put with _
            tag.getNames().forEach(s -> {
                s = s.toLowerCase().replaceAll(" ", "_");
                tagsMap.computeIfAbsent(s, s1 -> new HashSet<>()).add(tag);
            });
        });
    }

    public Set<Tag> get(String name) {
        name = StringUtils.stripAccents(name);
        name = StringUtils.lowerCase(name);
        name = StringUtils.normalizeSpace(name);

        if (StringUtils.isBlank(name)) return Set.of();

        return tagsMap.getOrDefault(name, Set.of());
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
