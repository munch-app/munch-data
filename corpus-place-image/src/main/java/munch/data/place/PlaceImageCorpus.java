package munch.data.place;

import catalyst.utils.exception.DateCompareUtils;
import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.field.PlaceKey;
import corpus.images.ImageField;
import munch.data.place.collector.CollectedImage;
import munch.data.place.collector.ImageCollector;
import munch.data.place.processor.ImageProcessor;
import munch.data.place.processor.ProcessedImage;
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
public final class PlaceImageCorpus extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(PlaceImageCorpus.class);

    private final ImageCollector imageCollector;
    private final ImageProcessor imageProcessor;

    @Inject
    public PlaceImageCorpus(ImageCollector imageCollector, ImageProcessor imageProcessor) {
        super(logger);
        this.imageCollector = imageCollector;
        this.imageProcessor = imageProcessor;
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
        // One week update once unless there is less then 3 images
        if (!due(placeId)) return;

        List<CorpusData> dataList = new ArrayList<>();
        catalystClient.listCorpus(placeId).forEachRemaining(dataList::add);

        // Collect and process
        List<CollectedImage> collectedImages = imageCollector.parse(placeId, dataList);
        List<ProcessedImage> processedImages = imageProcessor.process(collectedImages);

        CorpusData imageData = new CorpusData(cycleNo);
        imageData.setCatalystId(placeId);
        imageData.getFields().addAll(parse(processedImages));
        corpusClient.put("Sg.Munch.PlaceImage", placeId, imageData);

        sleep(100);
        if (processed % 100 == 0) logger.info("Processed {} places", processed);
    }

    /**
     * @param processedImages image to parse to ImageField
     * @return List of ImageField
     */
    private List<ImageField> parse(List<ProcessedImage> processedImages) {
        List<ImageField> fieldList = new ArrayList<>();

        for (int i = 0; i < processedImages.size(); i++) {
            ProcessedImage image = processedImages.get(i);
            ImageField field = new ImageField(PlaceKey.image, String.valueOf(i));
            field.setSource(image.getImage().getSource());
            field.setImages(image.getImage().getImages());
        }

        return fieldList;
    }

    /**
     * @param placeId if place id is due
     * @return true if due for update
     */
    private boolean due(String placeId) {
        CorpusData imageData = catalystClient.getCorpus(placeId, "Sg.Munch.PlaceImage");
        if (imageData == null) return true;
        if (PlaceKey.image.getAll(imageData).size() < 3) return true;
        return DateCompareUtils.after(imageData.getBridgedDate(), Duration.ofDays(7));
    }
}
