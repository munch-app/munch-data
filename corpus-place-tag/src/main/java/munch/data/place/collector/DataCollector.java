package munch.data.place.collector;

import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.CorpusModule;
import corpus.data.CatalystClient;
import corpus.data.CorpusClient;
import corpus.data.CorpusData;
import corpus.data.DataModule;
import munch.data.dynamodb.DynamoModule;
import munch.data.place.group.ExplicitTagParser;
import munch.data.place.text.CollectedText;
import munch.data.place.text.TextCollector;
import munch.restful.core.JsonUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 18/2/2018
 * Time: 9:55 PM
 * Project: munch-data
 */
public abstract class DataCollector {
    private static final Set<String> BLOCKED_TAGS = Set.of("restaurant", "hawker", "halal", "coffeeshop");
    private static final Logger logger = LoggerFactory.getLogger(DataCollector.class);

    // Clients Required
    protected CorpusClient corpusClient;
    protected CatalystClient catalystClient;

    protected TextCollector textCollector;
    protected ExplicitTagParser tagParser;

    protected final FileWriter fileWriter;
    protected final CSVPrinter csvWriter;

    protected final String labelFileName;
    protected final Map<String, String> labelMapping = new HashMap<>();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected DataCollector(String tagFileName, String labelFileName) throws IOException {
        this.labelFileName = labelFileName;
        File file = new File(tagFileName);
        file.createNewFile();
        this.fileWriter = new FileWriter(file);
        this.csvWriter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
        csvWriter.printRecord("texts", "tags");
    }

    @Inject
    public void inject(CorpusClient corpusClient, CatalystClient catalystClient, TextCollector textCollector, ExplicitTagParser tagParser) {
        this.corpusClient = corpusClient;
        this.catalystClient = catalystClient;
        this.textCollector = textCollector;
        this.tagParser = tagParser;
    }

    public abstract void put(DataGroup dataGroup) throws IOException;

    public void run() throws IOException, InterruptedException {
        int processed = 0;
        int iterated = 0;
        Iterator<CorpusData> iterator = corpusClient.list("Sg.Munch.Place");
        while (iterator.hasNext()) {
            if (++iterated % 100 == 0) logger.info("Iterated {} places, Processed {} places", iterated, processed);
            CorpusData data = iterator.next();

            DataGroup dataGroup = collectGroup(data.getCatalystId());
            if (dataGroup == null) continue;

            put(dataGroup);
            Thread.sleep(4);
            processed++;
        }
        logger.info("Completed");
    }

    @Nullable
    public DataGroup collectGroup(String placeId) {
        List<CorpusData> dataList = new ArrayList<>();
        catalystClient.listCorpus(placeId).forEachRemaining(dataList::add);

        List<CollectedText> collectedTexts = textCollector.collect(placeId, dataList);
        if (collectedTexts.isEmpty()) return null;

        // Put all output tags
        List<String> labels = tagParser.getExplicits(dataList, 10);
        labels.removeAll(BLOCKED_TAGS);
        if (labels.isEmpty()) return null;

        return new DataGroup(collectedTexts, labels);
    }

    public class DataGroup {
        final List<CollectedText> collectedTexts;
        final List<String> labels;

        DataGroup(List<CollectedText> collectedTexts, List<String> labels) {
            this.collectedTexts = collectedTexts;
            this.labels = labels;
        }

        List<String> getTexts() {
            return collectedTexts.stream()
                    .flatMap(collectedText -> collectedText.getTexts().stream())
                    .map(s -> s.replace("\n", ""))
                    .collect(Collectors.toList());
        }

        String getTags() {
            return labels.stream()
                    .map(s -> labelMapping.computeIfAbsent(s, key -> key
                            .toLowerCase()
                            .replace(" ", "")
                            .replace("&", "")
                            .replace("-", "")))
                    .collect(Collectors.joining(" "));
        }
    }

    public void close() throws IOException {
        fileWriter.flush();
        fileWriter.close();
        csvWriter.close();

        File file = new File(labelFileName);
        JsonUtils.objectMapper.writeValue(file, labelMapping);
    }

    public static <T extends DataCollector> void run(Class<T> clazz) throws IOException, InterruptedException {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            logger.error("Uncaught Exceptions: ", e.getCause());
        });

        System.setProperty("services.corpus.data.url", "http://proxy.corpus.munch.space:8200");

        Injector injector = Guice.createInjector(new CorpusModule(), new DataModule(), new DynamoModule());
        DataCollector collector = injector.getInstance(clazz);
        collector.run();
        collector.close();
    }
}
