package munch.data.place;

import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.images.ImageCachedField;
import munch.data.place.image.ImageParser;
import munch.data.structure.SourcedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 3:22 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceImageCorpus extends CatalystEngine<CorpusData>{
    private static final Logger logger = LoggerFactory.getLogger(PlaceImageCorpus.class);

    private final ImageParser imageParser;

    @Inject
    public PlaceImageCorpus(ImageParser imageParser) {
        super(logger);
        this.imageParser = imageParser;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(1);
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return corpusClient.list("Sg.Munch.Place");
    }

    @Override
    protected void process(long cycleNo, CorpusData placeData, long processed) {
        String placeId = placeData.getCatalystId();
        List<CorpusData> dataList = new ArrayList<>();
        catalystClient.listCorpus(placeId).forEachRemaining(dataList::add);
        List<SourcedImage> images = imageParser.parse(placeData.getCatalystId(), dataList);

        CorpusData imageData = new CorpusData(cycleNo);
        imageData.setCatalystId(placeId);

        for (int i = 0; i < images.size(); i++) {
            SourcedImage image = images.get(i);

            ImageCachedField field = new ImageCachedField("Place.image", String.valueOf(i));
            field.setSource(image.getSource());
            field.setImages(null, null, image.getImages());
            imageData.put(field);
        }

        corpusClient.put("Sg.Munch.PlaceImage", placeId, imageData);
        sleep(100);
    }
}
