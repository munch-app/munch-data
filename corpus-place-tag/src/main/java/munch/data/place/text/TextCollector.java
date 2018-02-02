package munch.data.place.text;

import corpus.data.CorpusData;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 10:13 PM
 * Project: munch-data
 */
@Singleton
public final class TextCollector {

    private final CorpusCollector corpusCollector;
    private final ArticleCollector articleCollector;
    private final InstagramCollector instagramCollector;

    @Inject
    public TextCollector(CorpusCollector corpusCollector, ArticleCollector articleCollector, InstagramCollector instagramCollector) {
        this.corpusCollector = corpusCollector;
        this.articleCollector = articleCollector;
        this.instagramCollector = instagramCollector;
    }

    /**
     * @param list list of corpus
     * @return List of Place.Image can be empty
     */
    public List<CollectedText> collect(String placeId, List<CorpusData> list) {
        List<CollectedText> collectedTexts = new ArrayList<>();
        collectedTexts.addAll(corpusCollector.collect(placeId, list));
        collectedTexts.addAll(articleCollector.collect(placeId, list));
        collectedTexts.addAll(instagramCollector.collect(placeId, list));

        return collectedTexts;
    }
}
