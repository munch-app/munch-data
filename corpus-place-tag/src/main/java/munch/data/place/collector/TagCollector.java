package munch.data.place.collector;

import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.CorpusModule;
import corpus.data.CatalystClient;
import corpus.data.CorpusClient;
import corpus.data.CorpusData;
import corpus.data.DataModule;
import corpus.field.PlaceKey;
import corpus.utils.FieldCollector;
import munch.data.dynamodb.DynamoModule;
import munch.data.place.group.GroupTagDatabase;
import munch.data.utils.ScheduledThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.*;

/**
 * Created by: Fuxing
 * Date: 5/3/2018
 * Time: 9:41 AM
 * Project: munch-data
 */
@Singleton
public final class TagCollector {
    private static final Logger logger = LoggerFactory.getLogger(TagCollector.class);

    protected final CorpusClient corpusClient;
    protected final CatalystClient catalystClient;

    private final Map<String, Integer> tags = new HashMap<>();

    private final GroupTagDatabase tagDatabase;

    @Inject
    public TagCollector(CorpusClient corpusClient, CatalystClient catalystClient, GroupTagDatabase tagDatabase) {
        this.corpusClient = corpusClient;
        this.catalystClient = catalystClient;
        this.tagDatabase = tagDatabase;
    }

    public void run() throws IOException, InterruptedException {
        int processed = 0;
        int iterated = 0;
        Iterator<CorpusData> iterator = corpusClient.list("Sg.Munch.Place");
        while (iterator.hasNext()) {
            if (++iterated % 100 == 0) logger.info("Iterated {} places, Processed {} places", iterated, processed);
            CorpusData data = iterator.next();

            List<CorpusData> dataList = collect(data.getCatalystId());
            if (dataList.isEmpty()) continue;

            put(dataList);
            Thread.sleep(5);
            processed++;
        }
        logger.info("Completed");
    }

    public List<CorpusData> collect(String placeId) {
        List<CorpusData> dataList = new ArrayList<>();
        catalystClient.listCorpus(placeId).forEachRemaining(dataList::add);
        return dataList;
    }

    public void put(List<CorpusData> dataList) {
        FieldCollector fieldCollector = new FieldCollector(PlaceKey.tag);
        fieldCollector.addAll(dataList);
        for (String tag : fieldCollector.collect()) {
            tags.compute(tag.toLowerCase(), (s, integer) -> {
                if (integer == null) return 1;
                return integer + 1;
            });
        }
    }

    public void close() {
        // Print out all tags collected
        tags.forEach((s, integer) -> {
            System.out.println(tagDatabase.has(s) + "," + s + "," + integer);
        });
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.setProperty("services.corpus.data.url", "http://proxy.corpus.munch.space:8200");

        Injector injector = Guice.createInjector(new CorpusModule(), new DataModule(), new DynamoModule());
        TagCollector collector = injector.getInstance(TagCollector.class);
        collector.run();
        collector.close();
        ScheduledThreadUtils.shutdown();
    }
}
