package munch.data.place;

import catalyst.utils.exception.DateCompareUtils;
import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.field.PlaceKey;
import munch.data.place.text.CollectedText;
import munch.data.place.text.TextCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
    private final TextCollector textCollector;

    @Inject
    public PlaceImageCorpus(TextCollector textCollector) {
        super(logger);
        this.textCollector = textCollector;
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
        List<CollectedText> collectedTexts = textCollector.parse(placeId, dataList);

        CorpusData imageData = new CorpusData(cycleNo);
        imageData.setCatalystId(placeId);
        imageData.put("Sg.Munch.PlaceImage.version", VERSION);
        corpusClient.put("Sg.Munch.PlaceImage", placeId, imageData);


        sleep(100);
        if (processed % 100 == 0) logger.info("Processed {} places", processed);
    }


    /**
     * @param placeId if place id is due
     * @return true if due for update
     */
    private boolean due(String placeId) {
        CorpusData imageData = catalystClient.getCorpus(placeId, "Sg.Munch.PlaceTag");
        if (imageData == null) return true;
        if (PlaceKey.image.getAll(imageData).size() < 3) {
            // If less then 3 photos, 1 day expiry date
            return DateCompareUtils.after(imageData.getBridgedDate(), Duration.ofDays(1));
        }
        return DateCompareUtils.after(imageData.getBridgedDate(), Duration.ofDays(7));
    }
}
