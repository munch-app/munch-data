package munch.data.place;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.place.parser.*;
import munch.data.structure.Place;

import javax.inject.Inject;
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
    private static final String[] PRIORITY_CORPUSES = new String[]{
            "Sg.MunchSheet.FranchisePlace",
            "Sg.MunchSheet.PlaceInfo",
    };

    private final PriceParser priceParser;
    private final LocationParser locationParser;
    private final TagParser tagParser;

    private final HourParser hourParser;
    private final ImageParser imageParser;
    private final RankingParser rankingParser;

    @Inject
    public PlaceParser(PriceParser priceParser, LocationParser locationParser, TagParser tagParser, HourParser hourParser, ImageParser imageParser, RankingParser rankingParser) {
        this.priceParser = priceParser;
        this.locationParser = locationParser;
        this.tagParser = tagParser;
        this.hourParser = hourParser;
        this.imageParser = imageParser;
        this.rankingParser = rankingParser;
    }

    public Place parse(List<CorpusData> list) {
        Place place = new Place();
        place.setId(list.get(0).getCatalystId());

        place.setName(collectMax(list, PRIORITY_CORPUSES, PlaceKey.name));
        place.setPhone(collectMax(list, PRIORITY_CORPUSES, PlaceKey.phone));
        place.setWebsite(collectMax(list, PRIORITY_CORPUSES, PlaceKey.website));
        place.setDescription(collectMax(list, PRIORITY_CORPUSES, PlaceKey.description));

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
}
