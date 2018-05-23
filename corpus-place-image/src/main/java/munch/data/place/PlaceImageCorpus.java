package munch.data.place;

import catalyst.utils.exception.DateCompareUtils;
import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.field.MetaKey;
import corpus.field.PlaceKey;
import corpus.images.ImageField;
import munch.data.place.collector.CollectedImage;
import munch.data.place.collector.CorpusCollector;
import munch.data.place.collector.ImageCollector;
import munch.data.place.processor.ImageListBuilder;
import munch.data.place.processor.ImageProcessor;
import munch.data.place.processor.MenuProcessor;
import munch.data.place.processor.ProcessedImage;
import munch.finn.FinnLabel;
import munch.finn.RawLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 3:22 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceImageCorpus extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(PlaceImageCorpus.class);

    private static final String VERSION = "2018-04-27";
    private final ImageCollector imageCollector;
    private final ImageProcessor imageProcessor;

    private final MenuProcessor menuProcessor;

    private final AirtableCounter airtableCounter;

    @Inject
    public PlaceImageCorpus(ImageCollector imageCollector, ImageProcessor imageProcessor, MenuProcessor menuProcessor, AirtableCounter airtableCounter) {
        super(logger);
        this.imageCollector = imageCollector;
        this.imageProcessor = imageProcessor;
        this.menuProcessor = menuProcessor;
        this.airtableCounter = airtableCounter;
    }

    @Override
    protected long loadCycleNo() {
        return System.currentTimeMillis();
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(13);
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return corpusClient.list("Sg.Munch.Place");
    }

    @Override
    protected void process(long cycleNo, CorpusData placeData, long processed) {
        String placeId = placeData.getCatalystId();
        // One week update once unless there is less then 3 images
        CorpusData imageData = catalystClient.getCorpus(placeId, "Sg.Munch.Place.Image");
        if (isDue(imageData)) {
            imageData = parse(placeId, cycleNo);
            corpusClient.put("Sg.Munch.Place.Image", placeId, imageData);
        }

        airtableCounter.add(imageData);

        sleep(80);
        if (processed % 100 == 0) logger.info("Processed {} places", processed);
    }

    @Override
    protected void postCycle(long cycleNo) {
        airtableCounter.finish(Duration.ofMillis(1000));
        super.postCycle(cycleNo);
    }

    @Override
    protected void deleteCycle(long cycleNo) {
        super.deleteCycle(cycleNo);
        corpusClient.deleteBefore("Sg.Munch.Place.Image", cycleNo - Duration.ofDays(7).toMillis());
    }

    private CorpusData parse(String placeId, long cycleNo) {
        List<CorpusData> dataList = new ArrayList<>();
        catalystClient.listCorpus(placeId).forEachRemaining(dataList::add);

        // Collect and process
        List<CollectedImage> collectedImages = imageCollector.collect(placeId, dataList);
        List<ProcessedImage> processedImages = imageProcessor.process(collectedImages);

        CorpusData imageData = new CorpusData(cycleNo);
        imageData.setCatalystId(placeId);
        imageData.getFields().addAll(parse(ImageListBuilder.select(processedImages)));
        imageData.put(MetaKey.version, VERSION);
        imageData.put(PlaceKey.id, placeId);
        corpusClient.put("Sg.Munch.Place.Image", placeId, imageData);

        // Put PlaceMenu collected from images
        processedImages.addAll(selectMenus(dataList));
        menuProcessor.put(placeId, processedImages);
        return imageData;
    }

    private static List<ProcessedImage> selectMenus(List<CorpusData> list) {
        return CorpusCollector.collect(list, "Place.Image.menu")
                .stream()
                .map(collectedImage -> {
                    ProcessedImage processedImage = new ProcessedImage();
                    processedImage.setImage(collectedImage);
                    FinnLabel finnLabel = new FinnLabel();
                    finnLabel.setOutput(Map.of("menu", 1.0f));
                    finnLabel.setRawLabel(new RawLabel());
                    processedImage.setFinnLabel(finnLabel);
                    return processedImage;
                })
                .collect(Collectors.toList());
    }

    /**
     * @param processedImages image to parse to ImageField
     * @return List of ImageField
     */
    private static List<ImageField> parse(List<ProcessedImage> processedImages) {
        List<ImageField> fieldList = new ArrayList<>();

        for (int i = 0; i < processedImages.size(); i++) {
            ProcessedImage image = processedImages.get(i);
            ImageField field = new ImageField(PlaceKey.image, String.valueOf(i));
            field.setSource(image.getImage().getSource());
            field.setSourceId(image.getImage().getSourceId());
            field.setSourceName(image.getImage().getSourceName());
            field.setSourceUrl(image.getImage().getSourceUrl());

            field.setSourceContentTitle(image.getImage().getSourceContentTitle());
            field.setSourceContentUrl(image.getImage().getSourceContentUrl());
            field.setImages(image.getImage().getImages());
            fieldList.add(field);
        }

        return fieldList;
    }

    /**
     * @return true if due for update
     */
    private boolean isDue(CorpusData imageData) {
        if (imageData == null) return true;
        if (!VERSION.equals(MetaKey.version.getValue(imageData))) return true;

        if (PlaceKey.image.getAll(imageData).size() < 3) {
            // If less then 3 photos, 2 day expiry date
            return DateCompareUtils.after(imageData.getBridgedDate(), Duration.ofDays(2));
        }
        return DateCompareUtils.after(imageData.getBridgedDate(), Duration.ofDays(4));
    }
}
