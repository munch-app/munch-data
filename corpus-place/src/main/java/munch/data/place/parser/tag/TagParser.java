package munch.data.place.parser.tag;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.place.parser.AbstractParser;
import munch.data.structure.Place;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 10:12 PM
 * Project: munch-data
 */
@Singleton
public final class TagParser extends AbstractParser<Place.Tag> {
    private final GroupTagDatabase groupTagDatabase;
    private final ImplicitTagParser implicitParser;

    @Inject
    public TagParser(GroupTagDatabase groupTagDatabase, ImplicitTagParser implicitParser) {
        this.groupTagDatabase = groupTagDatabase;
        this.implicitParser = implicitParser;
    }

    /**
     * @param place read-only place data
     * @param list  list of corpus data to parse from
     * @return Place.Tag, tags must all be in lowercase
     */
    @Override
    public Place.Tag parse(Place place, List<CorpusData> list) {
        List<String> explicits = parseExplicits(list);
        List<String> implicits = parseImplicits(place, explicits, list);

        Place.Tag tag = new Place.Tag();
        tag.setExplicits(explicits);
        tag.setImplicits(implicits);
        return tag;
    }

    private List<String> parseExplicits(List<CorpusData> list) {
        Set<GroupTag> groupTags = groupTagDatabase.findTags(collectValue(list, PlaceKey.tag));

        List<String> tags = new ArrayList<>();
        tags.addAll(findGroups(groupTags, 1));
        tags.addAll(findGroups(groupTags, 2));
        tags.addAll(findGroups(groupTags, 3));

        // If no tags, restaurant is the default
        if (tags.isEmpty()) {
            return Collections.singletonList("restaurant");
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

    private List<String> parseImplicits(Place place, Collection<String> explicitTags, List<CorpusData> list) {
        return implicitParser.parse(place, explicitTags, list);
    }

    /**
     * @param groupTags group of tags to search in
     * @param groupNo   groupNo
     * @return lowercase groups of tags
     */
    private List<String> findGroups(Set<GroupTag> groupTags, int groupNo) {
        return groupTags.stream()
                .filter(groupTag -> groupTag.getGroupNo() == groupNo)
                .sorted(Comparator.comparingInt(GroupTag::getOrder))
                .limit(2)
                // Must be lowercase
                .map(groupTag -> groupTag.getName().toLowerCase())
                .collect(Collectors.toList());
    }

}
