package munch.data.place;

import catalyst.utils.exception.DateCompareUtils;
import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.field.MetaKey;
import corpus.field.PlaceKey;
import corpus.images.ImageField;
import munch.data.place.collector.CollectedImage;
import munch.data.place.collector.ImageCollector;
import munch.data.place.processor.ImageListBuilder;
import munch.data.place.processor.ImageProcessor;
import munch.data.place.processor.MenuProcessor;
import munch.data.place.processor.ProcessedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.*;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 3:22 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceImageCorpus extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(PlaceImageCorpus.class);
    private static final Set<String> EXPLICIT_SOURCES = Set.of("munch-franchise", "munch-place-info");

    private static final String VERSION = "2018-01-31";
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
    protected Duration cycleDelay() {
        return Duration.ofHours(16);
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

    private CorpusData parse(String placeId, long cycleNo) {
        List<CorpusData> dataList = new ArrayList<>();
        catalystClient.listCorpus(placeId).forEachRemaining(dataList::add);

        // Collect and process
        List<CollectedImage> collectedImages = imageCollector.collect(placeId, dataList);
        List<ProcessedImage> processedImages = imageProcessor.process(collectedImages);

        // Put PlaceMenu collected from images
        menuProcessor.put(placeId, processedImages);

        CorpusData imageData = new CorpusData(cycleNo);
        imageData.setCatalystId(placeId);
        imageData.getFields().addAll(selectImages(processedImages));
        imageData.put(MetaKey.version, VERSION);
        corpusClient.put("Sg.Munch.Place.Image", placeId, imageData);
        return imageData;
    }

    private static List<ImageField> selectImages(List<ProcessedImage> processedImages) {
        ImageListBuilder builder = new ImageListBuilder(processedImages);
        // Select from explicit sources first
        builder.supply(stream -> stream
                .filter(PlaceImageCorpus::isExplicit)
                .sorted(Comparator.comparingLong(ImageListBuilder::sortSize)
                        .thenComparingDouble(ImageListBuilder::sortOutput)));

        // Select 3 food image if existing is less then 3, Sorted by Place.image, then score
        builder.supply(current -> current.size() < 3, stream -> stream
                .filter(image -> !isExplicit(image) && image.isOutput("food", 0.8f))
                .sorted(Comparator.comparingInt(ImageListBuilder::sortFrom)
                        .thenComparingLong(ImageListBuilder::sortSize)
                        .thenComparingDouble(ImageListBuilder::sortOutput)
                ).limit(3));

        // Select 1 place image, Sorted by Place.image, then score
        builder.supply(stream -> stream
                .filter(image -> image.isOutput("place", 0.8f))
                .sorted(Comparator.comparingInt(ImageListBuilder::sortFrom)
                        .thenComparingLong(ImageListBuilder::sortSize)
                        .thenComparingDouble(ImageListBuilder::sortOutput)
                ).limit(1));

        return parse(builder.collect());
    }

    /**
     * @param image processed image
     * @return true if source of image is explicit
     */
    private static boolean isExplicit(ProcessedImage image) {
        if (image.getImage().getSource() == null) return false;
        return EXPLICIT_SOURCES.contains(image.getImage().getSource());
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
        if (PlaceKey.image.getAll(imageData).size() < 3) {
            // If less then 3 photos, 2 day expiry date
            return DateCompareUtils.after(imageData.getBridgedDate(), Duration.ofDays(2));
        }
        return DateCompareUtils.after(imageData.getBridgedDate(), Duration.ofDays(3));
    }
}
