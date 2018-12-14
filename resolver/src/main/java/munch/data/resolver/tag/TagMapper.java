package munch.data.resolver.tag;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import munch.data.client.TagClient;
import munch.data.place.Place;
import munch.data.tag.Tag;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.io.IOException;
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

    private Set<String> postfixes;
    private Set<String> prefixes;
    private Set<String> blacklist;
    private Set<String> divider;

    @Inject
    public TagMapper(TagClient tagClient) throws IOException {
        postfixes = openResource("tag-postfix.txt");
        prefixes = openResource("tag-prefix.txt");
        blacklist = openResource("tag-blacklist.txt");
        divider = openResource("tag-divider.txt");


        tagClient.iterator().forEachRemaining(tag -> {
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
        name = StringUtils.stripAccents(name);
        name = StringUtils.lowerCase(name);
        name = StringUtils.normalizeSpace(name);

        if (StringUtils.isBlank(name)) return Set.of();

        Set<Tag> tags = tagsMap.get(name);
        if (tags != null) return tags;

        // Keep track all the versions
        Set<String> versions = getVersions(name);
        Map<String, Set<Tag>> mapped = new HashMap<>();

        versions.forEach(s -> {
            Set<Tag> set = tagsMap.get(s);
            if (set == null) return;

            mapped.put(s, set);
        });

        if (mapped.isEmpty()) return Set.of();

        // Get the longest version reduced Tag, to ensure reliability of tag
        return mapped.entrySet().stream()
                .min((o1, o2) -> Integer.compare(o2.getKey().length(), o1.getKey().length()))
                .map(Map.Entry::getValue)
                .orElse(Set.of());
    }

    /**
     * @param text to reduce into
     * @return multiple versions of the tag
     */
    private Set<String> getVersions(String text) {
        Set<String> versions = new HashSet<>();
        versions.add(text);

        // Replace all Dividers
        divider.forEach(s -> {
            versions.forEach(tag -> {
                tag = text.replace(s, " ");
                tag = tag.trim();
                if (StringUtils.isBlank(tag)) return;

                versions.add(StringUtils.normalizeSpace(tag));
            });
        });

        // Trim Prefix
        prefixes.forEach(s -> {
            versions.forEach(tag -> {
                tag = StringUtils.removeStart(tag, s);
                tag = tag.trim();
                if (StringUtils.isBlank(tag)) return;

                versions.add(StringUtils.normalizeSpace(tag));
            });
        });

        // Trim Postfix
        postfixes.forEach(s -> {
            versions.forEach(tag -> {
                tag = StringUtils.removeEnd(tag, s);
                tag = tag.trim();
                if (StringUtils.isBlank(tag)) return;

                versions.add(StringUtils.normalizeSpace(tag));
            });
        });

        // Remove all blacklisted
        versions.removeAll(blacklist);

        return versions;
    }

    /**
     * Convert Collections of Tag into list of Place.Tag
     * - Distinct & Ordered as input
     */
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

    private static Set<String> openResource(String resource) throws IOException {
        URL url = Resources.getResource(resource);
        Set<String> tags = new HashSet<>();
        Resources.readLines(url, Charset.defaultCharset()).forEach(s -> {
            if (StringUtils.isBlank(s)) return;
            tags.add(s.toLowerCase());
        });
        return ImmutableSet.copyOf(tags);
    }
}
