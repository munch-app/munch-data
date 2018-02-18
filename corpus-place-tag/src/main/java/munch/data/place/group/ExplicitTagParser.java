package munch.data.place.group;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import corpus.utils.FieldCollector;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 10:12 PM
 * Project: munch-data
 */
@Singleton
public final class ExplicitTagParser {
    private final GroupTagDatabase groupTagDatabase;

    @Inject
    public ExplicitTagParser(GroupTagDatabase groupTagDatabase) {
        this.groupTagDatabase = groupTagDatabase;
    }

    /**
     * @param list list
     * @return all possible tags of place
     */
    public List<String> getAll(List<CorpusData> list) {
        FieldCollector fieldCollector = new FieldCollector(PlaceKey.tag);
        fieldCollector.addAll(list);
        return fieldCollector.collect();
    }

    /**
     * @param list list of corpus data to parse from
     * @return Place.Tag, tags must all be in lowercase
     */
    public List<String> getExplicits(List<CorpusData> list) {
        return getExplicits(list, 2);
    }

    public List<String> getExplicits(List<CorpusData> list, int limits) {
        FieldCollector fieldCollector = new FieldCollector(PlaceKey.tag);
        fieldCollector.addAll(list);

        Set<GroupTag> groupTags = groupTagDatabase.findTags(fieldCollector.collect());

        List<String> tags = new ArrayList<>();
        tags.addAll(findGroups(groupTags, 1, limits));
        tags.addAll(findGroups(groupTags, 2, limits));
        tags.addAll(findGroups(groupTags, 3, limits));

        // If no tags, restaurant is the default
        if (tags.isEmpty()) {
            List<String> singleton = new ArrayList<>();
            singleton.add("restaurant");
            return singleton;
        }

        // Remove Restaurant if Hawker or Coffeeshop exists
        removeConflicts(tags);
        return tags;
    }

    private static void removeConflicts(List<String> tags) {
        if (tags.contains("hawker") || tags.contains("coffeeshop")) {
            tags.remove("restaurant");
        }
    }

    /**
     * @param groupTags group of tags to search in
     * @param groupNo   groupNo
     * @return lowercase groups of tags
     */
    private List<String> findGroups(Set<GroupTag> groupTags, int groupNo, int limit) {
        return groupTags.stream()
                .filter(groupTag -> groupTag.getGroupNo() == groupNo)
                .sorted(Comparator.comparingInt(GroupTag::getOrder))
                .limit(limit)
                // Must be lowercase
                .map(groupTag -> groupTag.getName().toLowerCase())
                .collect(Collectors.toList());
    }
}
