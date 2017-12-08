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
        double ranking = 0;
        for (CorpusData data : list) {
            if (data.getCorpusName().equals("Global.MunchArticle.Article")) {
                ranking += 10;
            } else {
                ranking += 1;
            }
        }
        if (!place.getImages().isEmpty()) {
            if (place.getImages().size() == 1 && place.getImages().get(0).getSource().equals("munch-image-placeholder")) {
                ranking += 500;
            } else {
                ranking += 1000;
            }
        }
        return ranking;
    }
}
