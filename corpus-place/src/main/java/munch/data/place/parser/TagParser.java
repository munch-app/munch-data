package munch.data.place.parser;

import catalyst.utils.LatLngUtils;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.place.parser.location.LocationDatabase;
import munch.data.place.parser.tag.GroupTag;
import munch.data.place.parser.tag.GroupTagDatabase;
import munch.data.structure.Place;

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
public final class TagParser extends AbstractParser<Place.Tag> {
    private final GroupTagDatabase groupTagDatabase;
    private final LocationDatabase locationDatabase;

    @Inject
    public TagParser(GroupTagDatabase groupTagDatabase, LocationDatabase locationDatabase) {
        this.groupTagDatabase = groupTagDatabase;
        this.locationDatabase = locationDatabase;
    }

    /**
     *
     * @param place read-only place data
     * @param list  list of corpus data to parse from
     * @return Place.Tag, tags must all be in lowercase
     */
    @Override
    public Place.Tag parse(Place place, List<CorpusData> list) {
        Place.Tag tag = new Place.Tag();
        tag.setExplicits(parseExplicits(list));
        tag.setImplicits(parseImplicits(place.getLocation(), list));
        return tag;
    }

    private List<String> parseExplicits(List<CorpusData> list) {
        Set<GroupTag> groupTags = groupTagDatabase.findTags(collectValue(list, PlaceKey.tag));

        List<String> tags = new ArrayList<>();
        tags.addAll(findGroups(groupTags, 1));
        tags.addAll(findGroups(groupTags, 2));
        tags.addAll(findGroups(groupTags, 3));

        if (!tags.contains("restaurant")) {
            tags.add(0, "restaurant");
        }
        return tags;
    }

    private List<String> parseImplicits(Place.Location location, List<CorpusData> list) {
        List<String> tags = new ArrayList<>();

        LatLngUtils.LatLng latLng = LatLngUtils.parse(location.getLatLng());
        tags.addAll(locationDatabase.findTags(latLng.getLat(), latLng.getLng()));

        return tags.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
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
