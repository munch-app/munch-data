package munch.data.place.parser.tag;

import catalyst.utils.LatLngUtils;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.place.parser.AbstractParser;
import munch.data.place.parser.location.LocationDatabase;
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
    private final ImplicitParser implicitParser;

    @Inject
    public TagParser(GroupTagDatabase groupTagDatabase, LocationDatabase locationDatabase) {
        this.groupTagDatabase = groupTagDatabase;
        this.implicitParser = new ImplicitParser(locationDatabase);
    }

    /**
     * @param place read-only place data
     * @param list  list of corpus data to parse from
     * @return Place.Tag, tags must all be in lowercase
     */
    @Override
    public Place.Tag parse(Place place, List<CorpusData> list) {
        Place.Tag tag = new Place.Tag();
        tag.setExplicits(parseExplicits(list));
        tag.setImplicits(parseImplicits(place, list));
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

    private List<String> parseImplicits(Place place, List<CorpusData> list) {
        return implicitParser.parse(place, list);
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

    private class ImplicitParser {
        private final LocationDatabase locationDatabase;

        private ImplicitParser(LocationDatabase locationDatabase) {
            this.locationDatabase = locationDatabase;
        }

        private List<String> parse(Place place, List<CorpusData> list) {
            List<String> tags = new ArrayList<>();

            // Parse all information to add
            tags.addAll(parseLocation(place));

            return tags.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
        }

        private Set<String> parseLocation(Place place) {
            LatLngUtils.LatLng latLng = LatLngUtils.parse(place.getLocation().getLatLng());
            return locationDatabase.findTags(latLng.getLat(), latLng.getLng());
        }
    }
}
