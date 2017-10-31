package munch.data.place.parser;

import corpus.data.CorpusData;
import munch.data.structure.Place;

import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 10:14 PM
 * Project: munch-data
 */
@Singleton
public final class RankingParser extends AbstractParser<Double> {

    @Override
    public Double parse(Place place, List<CorpusData> list) {
        double ranking = list.size();
        if (!place.getImages().isEmpty()) ranking += 1000;
        return ranking;
    }
}
