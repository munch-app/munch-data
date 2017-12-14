package munch.data.place.collector;

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
public final class ImageCollector {

    private final CorpusCollector corpusCollector;
    private final ArticleCollector articleCollector;
    private final InstagramCollector instagramCollector;

    @Inject
    public ImageCollector(CorpusCollector corpusCollector, ArticleCollector articleCollector, InstagramCollector instagramCollector) {
        this.corpusCollector = corpusCollector;
        this.articleCollector = articleCollector;
        this.instagramCollector = instagramCollector;
    }

    /**
     * @param list list of corpus
     * @return List of Place.Image can be empty
     */
    public List<CollectedImage> parse(String placeId, List<CorpusData> list) {
        List<CollectedImage> collectedImages = new ArrayList<>();
        collectedImages.addAll(corpusCollector.collect(placeId, list));
        collectedImages.addAll(articleCollector.collect(placeId, list));
        collectedImages.addAll(instagramCollector.collect(placeId, list));

        return collectedImages;
    }
}
