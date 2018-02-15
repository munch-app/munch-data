package munch.data.place.ml;

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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 6/2/18
 * Time: 3:00 PM
 * Project: munch-data
 */
@Singleton
public final class TrainingPipelineCorpus extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(TrainingPipelineCorpus.class);
    private static final Set<String> BLOCKED_TAGS = Set.of("restaurant", "hawker", "halal");

    private final TextCollector textCollector;
    private final TopicAnalysis topicAnalysis;

    private final ExplicitTagParser tagParser;

    @Inject
    public TrainingPipelineCorpus(TextCollector textCollector, TopicAnalysis topicAnalysis, ExplicitTagParser tagParser) {
        super(logger);
        this.textCollector = textCollector;
        this.topicAnalysis = topicAnalysis;
        this.tagParser = tagParser;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofDays(1);
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return corpusClient.list("Sg.Munch.Place");
    }

    @Override
    protected void process(long cycleNo, CorpusData data, long processed) {
        String placeId = data.getCatalystId();

        // Collect DataList for Tags & Topics
        List<CorpusData> dataList = new ArrayList<>();
        catalystClient.listCorpus(placeId).forEachRemaining(dataList::add);

        List<CollectedText> collectedTexts = textCollector.collect(placeId, dataList);

        CorpusData trainingData = new CorpusData(cycleNo);

        // Put all output tags
        List<String> tags = tagParser.parse(dataList);
        tags.removeAll(BLOCKED_TAGS);
        if (tags.isEmpty()) return;
        tags.forEach(tag -> trainingData.put(TrainingPipelineKey.output, tag));

        // Put all input tags
        Map<String, Integer> topics = analyzeTopic(collectedTexts);
        if (topics.isEmpty()) return;

        topics.forEach((tag, count) -> {
            trainingData.put(TrainingPipelineKey.input.create(tag, count));
        });

        // Technically don't need to use the same id as place id
        corpusClient.put("Sg.Munch.PlaceTagTraining", placeId, trainingData);

        sleep(100);
        if (processed % 100 == 0) logger.info("Processed {} places", processed);
    }

    private Map<String, Integer> analyzeTopic(List<CollectedText> collectedTexts) {
        List<String> texts = collectedTexts.stream()
                .flatMap(collected -> collected.getTexts().stream())
                .collect(Collectors.toList());
        try {
            if (texts.isEmpty()) return Map.of();
            List<Map<String, Integer>> topics = topicAnalysis.apply(texts, 1, 50);
            if (topics.isEmpty()) return Map.of();
            return topics.get(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void deleteCycle(long cycleNo) {
        super.deleteCycle(cycleNo);
        corpusClient.deleteBefore("Sg.Munch.PlaceTagTraining", cycleNo);
    }
}
