package munch.data.place;

import com.google.common.collect.ImmutableList;
import com.typesafe.config.Config;
import corpus.data.CorpusData;
import corpus.field.AbstractKey;
import corpus.field.PlaceKey;
import munch.data.place.matcher.NameNormalizer;
import munch.data.place.parser.*;
import munch.data.structure.Place;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 11:33 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceParser extends AbstractParser<Place> {
    private static final Pattern HTTP_PATTERN = Pattern.compile("^https?://.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile(".*(65)?\\s?(?<g1>[0-9]{4})\\s?(?<g2>[0-9]{4}).*", Pattern.CASE_INSENSITIVE);

    private final List<String> priorityNames;

    private final NameNormalizer nameNormalizer;

    private final PriceParser priceParser;
    private final LocationParser locationParser;
    private final ReviewParser reviewParser;
    private final TagParser tagParser;

    private final HourParser hourParser;
    private final ImageParser imageParser;
    private final RankingParser rankingParser;

    @Inject
    public PlaceParser(Config config, NameNormalizer nameNormalizer, PriceParser priceParser, LocationParser locationParser, ReviewParser reviewParser, TagParser tagParser, HourParser hourParser, ImageParser imageParser, RankingParser rankingParser) {
        this.priorityNames = ImmutableList.copyOf(config.getStringList("place.priority"));
        this.nameNormalizer = nameNormalizer;
        this.priceParser = priceParser;
        this.locationParser = locationParser;
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

        place.setName(collectName(list));
        place.setPhone(collectPhone(list));
        place.setWebsite(collectWebsite(list));
        place.setDescription(collectDescription(list));

        // LocationParser is mandatory
        place.setLocation(locationParser.parse(place, list));
        if (place.getLocation() == null) return null;

        // Theses are no dependencies parsers
        place.setPrice(priceParser.parse(place, list));
        place.setHours(hourParser.parse(place, list));
        place.setReview(reviewParser.parse(place, list));

        // TagParser depend on HourParser & LocationParser
        place.setTag(tagParser.parse(place, list));
        // RankingParser depend on ImageParser
        place.setRanking(rankingParser.parse(place, list));
        // ImageParser depend on TagParser
        place.setImages(imageParser.parse(place, list));

        place.setCreatedDate(findCreatedDate(list));
        place.setUpdatedDate(new Date());
        return place;
    }

    private Date findCreatedDate(List<CorpusData> list) {
        return list.stream()
                .map(CorpusData::getCreatedDate)
                .min(Date::compareTo)
                .orElseThrow(NullPointerException::new);
    }

    private String collectName(List<CorpusData> list) {
        String name = collectMax(list, PlaceKey.name);
        // Normalize name first
        name = nameNormalizer.normalize(name);
        // Then capitalize fully name
        return WordUtils.capitalizeFully(name);
    }

    private String collectPhone(List<CorpusData> list) {
        String phone = collectMax(list, PlaceKey.phone);
        if (phone == null) return null;

        Matcher matcher = PHONE_PATTERN.matcher(phone);
        if (!matcher.matches()) return null;

        String g1 = matcher.group("g1");
        String g2 = matcher.group("g2");
        return "+65 " + g1 + " " + g2;
    }

    private String collectWebsite(List<CorpusData> list) {
        String website = collectMax(list, PlaceKey.website);
        if (website == null) return null;

        // Website cannot be facebook.com
        if (website.contains("facebook.com")) return null;
        if (HTTP_PATTERN.matcher(website).matches()) return website;
        return "http://" + website;
    }

    private String collectDescription(List<CorpusData> list) {
        String description = collectMax(list, PlaceKey.description);
        if (StringUtils.isBlank(description)) return null;
        return description.replaceAll(" {2,}", " ");
    }

    @Nullable
    @Override
    protected String collectMax(List<CorpusData> list, AbstractKey... keys) {
        return collectMax(list, priorityNames, keys);
    }
}
