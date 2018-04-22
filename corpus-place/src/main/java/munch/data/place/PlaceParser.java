package munch.data.place;

import corpus.data.CorpusData;
import corpus.field.AbstractKey;
import corpus.field.MetaKey;
import corpus.utils.FieldCollector;
import munch.data.place.graph.PlaceTree;
import munch.data.place.parser.*;
import munch.data.place.parser.hour.HourParser;
import munch.data.place.parser.location.LocationParser;
import munch.data.structure.Place;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 11:33 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceParser {
    private static final AbstractKey[] TIMESTAMP_KEYS = new AbstractKey[]{AbstractKey.of("Article.timestamp"), MetaKey.createdDate};
    private static final String version = "2018-03-30";

    private final NameParser nameParser;
    private final PhoneParser phoneParser;
    private final DescriptionParser descriptionParser;

    private final WebsiteParser websiteParser;
    private final MenuParser menuParser;

    private final PriceParser priceParser;
    private final LocationParser locationParser;
    private final ContainerParser containerParser;
    private final ReviewParser reviewParser;
    private final TagParser tagParser;

    private final HourParser hourParser;
    private final ImageParser imageParser;
    private final RankingParser rankingParser;

    @Inject
    public PlaceParser(NameParser nameParser, PhoneParser phoneParser, WebsiteParser websiteParser, DescriptionParser descriptionParser,
                       MenuParser menuParser, PriceParser priceParser, LocationParser locationParser, ContainerParser containerParser,
                       ReviewParser reviewParser, TagParser tagParser, HourParser hourParser, ImageParser imageParser, RankingParser rankingParser) {
        this.nameParser = nameParser;
        this.phoneParser = phoneParser;
        this.websiteParser = websiteParser;
        this.descriptionParser = descriptionParser;
        this.menuParser = menuParser;

        this.priceParser = priceParser;
        this.locationParser = locationParser;
        this.containerParser = containerParser;
        this.reviewParser = reviewParser;
        this.tagParser = tagParser;

        this.hourParser = hourParser;
        this.imageParser = imageParser;
        this.rankingParser = rankingParser;
    }

    /**
     * @param placeId   id of place
     * @param placeTree data
     * @param decay     whether data has decayed
     * @return Parsed Place, null if parsing failed
     */
    @Nullable
    public Place parse(String placeId, PlaceTree placeTree, boolean decay) {
        List<CorpusData> list = placeTree.getCorpusDataList();
        // Remove Sg.Munch.Place from influencing Parser
        list.removeIf(data -> data.getCorpusName().equals("Sg.Munch.Place"));

        Place place = new Place();
        place.setId(placeId);
        place.setVersion(version);

        place.setName(nameParser.parse(place, list));
        if (StringUtils.isBlank(place.getName())) return null;
        place.setAllNames(nameParser.parseAllNames(place, list));
        place.setPhone(phoneParser.parse(place, list));
        place.setDescription(descriptionParser.parse(place, list));

        place.setWebsite(websiteParser.parse(place, list));
        place.setMenuUrl(menuParser.parse(place, list));

        // LocationParser is mandatory
        place.setLocation(locationParser.parse(place, list));
        if (place.getLocation() == null) return null;

        // Theses are parers without dependencies
        place.setContainers(containerParser.parse(place, list));
        place.setPrice(priceParser.parse(place, list));
        place.setHours(hourParser.parse(place, list));
        place.setReview(reviewParser.parse(place, list));

        // TagParser depend on HourParser & LocationParser
        place.setTag(tagParser.parse(place, list));
        // ImageParser depend on TagParser
        place.setImages(imageParser.parse(place, list));
        // RankingParser depend on ImageParser
        place.setRanking(rankingParser.parse(place, list));

        place.setCreatedDate(findCreatedDate(list));
        place.setUpdatedDate(new Date());
        place.setOpen(!decay);
        return place;
    }

    /**
     * @param list list
     * @return earliest created date
     */
    private Date findCreatedDate(List<CorpusData> list) {
        Set<Long> timestamps = new HashSet<>();

        // Collect all created date
        list.forEach(data -> timestamps.add(data.getCreatedDate().getTime()));

        // Collect timestamp keys
        FieldCollector fieldCollector = new FieldCollector(TIMESTAMP_KEYS);
        fieldCollector.addAll(list);
        fieldCollector.collect().stream()
                .filter(s -> StringUtils.isNotBlank(s) && !s.equals("0"))
                .forEach(s -> {
                    try {
                        timestamps.add(Long.parseLong(s));
                    } catch (NumberFormatException ignored) {
                    }
                });

        return timestamps.stream()
                .min(Long::compareTo)
                .map(Date::new)
                .orElseThrow(NullPointerException::new);
    }
}
