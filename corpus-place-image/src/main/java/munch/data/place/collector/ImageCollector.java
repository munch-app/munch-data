package munch.data.place.collector;

import corpus.data.CorpusData;
import munch.data.place.AirtableDatabase;

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

    private final AirtableDatabase airtableDatabase;

    @Inject
    public ImageCollector(CorpusCollector corpusCollector, ArticleCollector articleCollector, InstagramCollector instagramCollector, AirtableDatabase airtableDatabase) {
        this.corpusCollector = corpusCollector;
        this.articleCollector = articleCollector;
        this.instagramCollector = instagramCollector;
        this.airtableDatabase = airtableDatabase;
    }

    /**
     * @param list list of corpus
     * @return List of Place.Image can be empty
     */
    public List<CollectedImage> collect(String placeId, List<CorpusData> list) {
        List<CollectedImage> collectedImages = new ArrayList<>();
        collectedImages.addAll(corpusCollector.collect(placeId, list));
        collectedImages.addAll(articleCollector.collect(placeId, list));
        collectedImages.addAll(instagramCollector.collect(placeId, list));

        // Remove images that are not approved from airtable
        collectedImages.removeIf(this::toRemove);

        return collectedImages;
    }

    public boolean toRemove(CollectedImage image) {
        return !airtableDatabase.allow(image.getSource(), image.getSourceId());
    }
}
