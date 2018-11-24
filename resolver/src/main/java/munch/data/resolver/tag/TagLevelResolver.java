package munch.data.resolver.tag;

import munch.data.tag.Tag;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 1/10/18
 * Time: 3:38 PM
 * Project: munch-data
 */
@Singleton
public final class TagLevelResolver {

    public List<Tag> resolve(Set<Tag> tags) {
        List<Tag> collector = new ArrayList<>();
        select(1, 1, tags, collector);
        select(2, 1, tags, collector);
        select(3, 2, tags, collector);

        // Add remaining provider to collector
        collector.addAll(tags);

        // if no tag found, restaurant will be returned
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
}
