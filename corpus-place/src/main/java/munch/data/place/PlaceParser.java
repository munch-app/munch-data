package munch.data.place;

import corpus.data.CorpusData;
import corpus.field.AbstractKey;
import corpus.field.PlaceKey;
import munch.data.place.parser.*;
import munch.data.structure.Place;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Date;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 11:33 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceParser extends AbstractParser {

    private final List<String> priorityNames;

    private final PriceParser priceParser;
    private final LocationParser locationParser;
    private final TagParser tagParser;

    private final HourParser hourParser;
    private final ImageParser imageParser;
    private final RankingParser rankingParser;

    @Inject
    public PlaceParser(@Named("place.priority") List<String> priorityNames, PriceParser priceParser, LocationParser locationParser, TagParser tagParser, HourParser hourParser, ImageParser imageParser, RankingParser rankingParser) {
        this.priorityNames = priorityNames;
        this.priceParser = priceParser;
        this.locationParser = locationParser;
        this.tagParser = tagParser;
        this.hourParser = hourParser;
        this.imageParser = imageParser;
        this.rankingParser = rankingParser;
    }

    /**
     * @param list list of CorpusData to use
     * @return Parsed Place, non-null
     */
    public Place parse(List<CorpusData> list) {
        Place place = new Place();
        place.setId(list.get(0).getCatalystId());

        place.setName(collectMax(list, PlaceKey.name));
        place.setPhone(collectMax(list, PlaceKey.phone));
        place.setWebsite(collectMax(list, PlaceKey.website));
        place.setDescription(collectMax(list, PlaceKey.description));

        // Nested Parsers
        place.setPrice(priceParser.parse(list));
        place.setLocation(locationParser.parse(list));
        place.setTag(tagParser.parse(list));

        place.setHours(hourParser.parse(list));
        place.setImages(imageParser.parse(list));

        place.setRanking(rankingParser.parse(list));
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

    @Nullable
    @Override
    protected String collectMax(List<CorpusData> list, AbstractKey... keys) {
        return collectMax(list, priorityNames, keys);
    }
}
