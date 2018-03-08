package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.FieldUtils;
import munch.data.structure.Place;

import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            ranking += getCorpusDataScore(data);
        }

        // Article TemplateIds
        ranking += getArticleScore(place, list);

        // Image
        ranking += getImageScore(place, list);

        // Negative scores
        ranking += getNegative(place, list);

        if (ranking < 0) return 0.0;
        if (ranking > 2000) return 2000.0;
        return ranking;
    }

    private double getCorpusDataScore(CorpusData data) {
        switch (data.getCorpusName()) {
            case "Sg.Munch.PlaceAward":
                return 20;

            case "Global.Facebook.Place":
            case "Global.Instagram.Location":
                return 10;
            case "Global.MunchArticle.Article":
                return 2;
            case "Sg.Munch.Place":
            default:
                return 1;

            // Utility Corpus gives no ranking
            case "Sg.Munch.PlaceImage": // Deprecated
            case "Sg.Munch.Place.Image":
            case "Sg.Munch.Place.Tag":
            case "Sg.MunchSheet.ConceptPlace":
                return 0;
        }
    }

    private double getArticleScore(Place place, List<CorpusData> list) {
        return list.stream()
                .filter(data -> data.getCorpusName().equals("Global.MunchArticle.Article"))
                .map(data -> FieldUtils.get(data, "Article.templateId"))
                .filter(Optional::isPresent)
                .map(field -> field.get().getValue())
                .collect(Collectors.toSet())
                .size() * 10;
    }

    private double getImageScore(Place place, List<CorpusData> list) {
        if (!place.getImages().isEmpty()) {
            return 1000;
        }
        return 0;
    }

    private double getNegative(Place place, List<CorpusData> list) {
        if (isFastFood(place)) return -200;
        return 0;
    }

    private boolean isFastFood(Place place) {
        return place.getTag().getExplicits().contains("fast food");
    }
}
