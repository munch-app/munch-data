package munch.data.place;

import corpus.data.CorpusData;
import munch.data.place.parser.HourParser;
import munch.data.place.parser.ImageParser;
import munch.data.place.parser.PriceParser;
import munch.data.place.parser.RankingParser;
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
public final class PlaceParser {
    private final HourParser hourParser;
    private final PriceParser priceParser;
    private final ImageParser imageParser;
    private final RankingParser rankingParser;

    @Inject
    public PlaceParser(HourParser hourParser, PriceParser priceParser, ImageParser imageParser, RankingParser rankingParser) {
        this.hourParser = hourParser;
        this.priceParser = priceParser;
        this.imageParser = imageParser;
        this.rankingParser = rankingParser;
    }


    public Place parse(List<CorpusData> list) {
        Place place = new Place();

        // Nested Parsers
        place.setHours(hourParser.parse(list));
        place.setPrice(priceParser.parse(list));
        place.setImages(imageParser.parse(list));
        place.setRanking(rankingParser.parse(list));
        return place;
    }
}
