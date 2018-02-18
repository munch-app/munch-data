package munch.data.place;

import catalyst.utils.exception.DateCompareUtils;
import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import munch.data.place.group.ExplicitTagParser;
import munch.data.place.text.CollectedText;
import munch.data.place.text.TextCollector;
import munch.data.place.topic.TopicAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
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
public final class PlaceTagCorpus extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(PlaceTagCorpus.class);

    private static final String VERSION = "2018-01-31";
    private final TextCollector textCollector;
    private final TopicAnalysis topicAnalysis;

    private final ExplicitTagParser tagParser;

    @Inject
    public PlaceTagCorpus(TextCollector textCollector, ExplicitTagParser tagParser, TopicAnalysis topicAnalysis) {
        super(logger);
        this.textCollector = textCollector;
        this.tagParser = tagParser;
        this.topicAnalysis = topicAnalysis;
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
        List<CollectedText> collectedTexts = textCollector.collect(placeId, dataList);
        CorpusData tagData = new CorpusData(cycleNo);
        tagData.setCatalystId(placeId);
        tagData.put("Sg.Munch.PlaceTag.version", VERSION);
        corpusClient.put("Sg.Munch.PlaceTag", placeId, tagData);


        sleep(100);
        if (processed % 100 == 0) logger.info("Processed {} places", processed);
    }

    private Map<String, Integer> analyzeTopic(List<CollectedText> collectedTexts) {
        List<String> texts = collectedTexts.stream()
                .flatMap(collected -> collected.getTexts().stream())
                .collect(Collectors.toList());
        try {
            return topicAnalysis.apply(texts, 1, 50).get(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param placeId if place id is due
     * @return true if due for update
     */
    private boolean due(String placeId) {
        CorpusData tagData = catalystClient.getCorpus(placeId, "Sg.Munch.PlaceTag");
        if (tagData == null) return true;
        return DateCompareUtils.after(tagData.getBridgedDate(), Duration.ofDays(7));
    }
}
