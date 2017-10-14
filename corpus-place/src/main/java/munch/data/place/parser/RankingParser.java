package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;

import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 10:14 PM
 * Project: munch-data
 */
@Singleton
public final class RankingParser extends AbstractParser {

    public double parse(List<CorpusData> list) {
        double ranking = list.size();
        if (hasAny(PlaceKey.image, list)) ranking += 1000;
        return ranking;
    }
}
