package munch.data.place;

import corpus.data.CorpusData;
import munch.data.place.parser.*;
import munch.data.structure.Place;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 11:33 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceParser extends AbstractParser {
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

        // TODO values with overrides

        // Nested Parsers
        place.setPrice(priceParser.parse(list));
        place.setLocation(locationParser.parse(list));
        place.setTag(tagParser.parse(list));

        place.setHours(hourParser.parse(list));
        place.setImages(imageParser.parse(list));
        place.setRanking(rankingParser.parse(list));
        return place;
    }
}
