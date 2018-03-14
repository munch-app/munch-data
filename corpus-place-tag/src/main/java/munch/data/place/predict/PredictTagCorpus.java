package munch.data.place.predict;

import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import munch.data.clients.PlaceCardClient;
import munch.data.place.text.CollectedText;
import munch.data.place.text.TextCollector;
import munch.data.structure.PlaceJsonCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 3:22 PM
 * Project: munch-data
 */
@Singleton
@Deprecated
public final class PredictTagCorpus extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(PredictTagCorpus.class);

    private final TextCollector textCollector;

    private final PlaceCardClient placeCardClient;
    private final PredictTagClient predictTagClient;

    @Inject
    public PredictTagCorpus(TextCollector textCollector, PlaceCardClient placeCardClient, PredictTagClient predictTagClient) {
        super(logger);
        this.textCollector = textCollector;
        this.placeCardClient = placeCardClient;
        this.predictTagClient = predictTagClient;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(18);
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

        // Collect
        List<CollectedText> collectedTexts = textCollector.collect(placeId, dataList);
        if (collectedTexts.isEmpty()) return;

        // Process if Able
        Map<String, Double> labels = predict(collectedTexts);
        if (labels != null) {
            counter.increment("Tagged");
            PlaceJsonCard card = new PlaceJsonCard("ugc_SuggestedTag_20180219", labels);
            placeCardClient.putIfChange(placeId, card);
        } else {
            placeCardClient.deleteIfNonNull("ugc_SuggestedTag_20180219", placeId);
        }

        sleep(100);
        if (processed % 100 == 0) logger.info("Processed {} places", processed);
    }

    @Nullable
    public Map<String, Double> predict(List<CollectedText> collectedTexts) {
        if (collectedTexts.isEmpty()) return null;
        List<String> texts = collectedTexts.stream()
                .flatMap(collectedText -> collectedText.getTexts().stream())
                .collect(Collectors.toList());
        Map<String, Double> labels = predictTagClient.predict(texts);
        if (labels.isEmpty()) return null;

        Map<String, Double> mapped = new HashMap<>();

        // Only map if > 0.3
        labels.forEach((key, value) -> {
            if (value > 0.5) {
//                groupTagDatabase.resolve(key).ifPresent(tag -> {
//                    mapped.put(tag, value);
//                });
            }
        });

        if (mapped.isEmpty()) return null;
        return mapped;
    }
}
