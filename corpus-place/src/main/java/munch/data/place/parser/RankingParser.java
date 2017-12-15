package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.FieldUtils;
import munch.data.structure.Place;
import munch.data.structure.SourcedImage;

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
        ranking += getArticleScore(list);

        // Image
        ranking += getImageScore(place);
        return ranking;
    }

    private double getCorpusDataScore(CorpusData data) {
        switch (data.getCorpusName()) {
            case "Global.Facebook.Place":
            case "Global.Instagram.Location":
                return 10;
            case "Global.MunchArticle.Article":
                return 2;
            case "Sg.Munch.Place":
            default:
                return 1;

            // Utility Corpus gives no ranking
            case "Sg.Munch.PlaceImage":
                return 0;
        }
    }

    private double getArticleScore(List<CorpusData> list) {
        return list.stream()
                .filter(data -> data.getCorpusName().equals("Global.MunchArticle.Article"))
                .map(data -> FieldUtils.get(data, "Article.templateId"))
                .filter(Optional::isPresent)
                .map(field -> field.get().getValue())
                .collect(Collectors.toSet())
                .size() * 10;
    }

    private double getImageScore(Place place) {
        if (!place.getImages().isEmpty()) {
            if (isPlaceholder(place.getImages())) return 500;
            return 1000;
        }
        return 0;
    }

    private boolean isPlaceholder(List<SourcedImage> images) {
        if (images.size() > 1) return false;
        String source = images.get(0).getSource();
        if (source == null) return false;
        return source.equals("munch-image-placeholder");
    }
}
