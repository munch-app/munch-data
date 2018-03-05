package munch.data.place;

import com.google.common.collect.ImmutableList;
import com.typesafe.config.Config;
import corpus.data.CorpusData;
import corpus.field.AbstractKey;
import munch.data.place.parser.*;
import munch.data.place.parser.hour.HourParser;
import munch.data.place.parser.location.LocationParser;
import munch.data.place.parser.tag.TagParser;
import munch.data.structure.Place;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 11:33 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceParser extends AbstractParser<Place> {
    private static final AbstractKey[] TIMESTAMP_KEYS = new AbstractKey[]{AbstractKey.of("Article.timestamp")};
    private static final String version = "2018-02-05";
    private final List<String> priorityNames;

    private final StatusParser statusParser;

    private final NameParser nameParser;
    private final PhoneParser phoneParser;
    private final WebsiteParser websiteParser;
    private final DescriptionParser descriptionParser;

    private final PriceParser priceParser;
    private final LocationParser locationParser;
    private final ContainerParser containerParser;
    private final ReviewParser reviewParser;
    private final TagParser tagParser;

    private final HourParser hourParser;
    private final ImageParser imageParser;
    private final RankingParser rankingParser;

    @Inject
    public PlaceParser(Config config, StatusParser statusParser, NameParser nameParser, PhoneParser phoneParser, WebsiteParser websiteParser, DescriptionParser descriptionParser,
                       PriceParser priceParser, LocationParser locationParser, ContainerParser containerParser, ReviewParser reviewParser, TagParser tagParser,
                       HourParser hourParser, ImageParser imageParser, RankingParser rankingParser) {
        this.priorityNames = ImmutableList.copyOf(config.getStringList("place.priority"));
        this.statusParser = statusParser;
        this.nameParser = nameParser;
        this.phoneParser = phoneParser;
        this.websiteParser = websiteParser;
        this.descriptionParser = descriptionParser;

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
     * @param list list of CorpusData to use
     * @return Parsed Place, null if parsing failed
     */
    @Override
    @Nullable
    public Place parse(Place place, List<CorpusData> list) {
        place.setId(list.get(0).getCatalystId());
        place.setVersion(version);

        // Check if anyone blocked the status
        if (!statusParser.parse(place, list)) return null;

        place.setName(nameParser.parse(place, list));
        if (StringUtils.isBlank(place.getName())) return null;
        place.setAllNames(nameParser.parseAllNames(place, list));
        place.setPhone(phoneParser.parse(place, list));
        place.setWebsite(websiteParser.parse(place, list));
        place.setDescription(descriptionParser.parse(place, list));

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
        return place;
    }

    /**
     * @param list list
     * @return earliest created date
     */
    private Date findCreatedDate(List<CorpusData> list) {
        Set<Long> timestamps = new HashSet<>();
        for (CorpusData data : list) {
            timestamps.add(data.getCreatedDate().getTime());
            collect(data, TIMESTAMP_KEYS).forEach(field -> {
                if (field.getValue().equals("0")) return;
                try {
                    timestamps.add(Long.parseLong(field.getValue()));
                } catch (NumberFormatException ignored) {
                }
            });
        }
        return timestamps.stream()
                .min(Long::compareTo)
                .map(Date::new)
                .orElseThrow(NullPointerException::new);
    }

    @Nullable
    @Override
    protected String collectMax(List<CorpusData> list, AbstractKey... keys) {
        return collectMax(list, priorityNames, keys);
    }
}
