package munch.data.place;

import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.field.MetaKey;
import corpus.field.PlaceKey;
import munch.data.place.collector.TagCollector;
import munch.data.place.group.PlaceTagCounter;
import munch.data.place.group.PlaceTagDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.*;

/**
 * Created by: Fuxing
 * Date: 8/3/18
 * Time: 4:48 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceTagCorpus extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(PlaceTagCorpus.class);

    private final TagCollector tagCollector;
    private final PlaceTagDatabase database;
    private PlaceTagCounter counter;

    @Inject
    public PlaceTagCorpus(TagCollector tagCollector, PlaceTagDatabase database) {
        super(logger);
        this.tagCollector = tagCollector;
        this.database = database;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(18);
    }

    @Override
    protected boolean preCycle(long cycleNo) {
        this.counter = new PlaceTagCounter();
        return super.preCycle(cycleNo);
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return corpusClient.list("Sg.Munch.Place");
    }

    @Override
    protected void process(long cycleNo, CorpusData data, long processed) {
        List<CorpusData> dataList = new ArrayList<>();
        corpusClient.list(data.getCatalystId()).forEachRemaining(dataList::add);

        TagCollector.Group group = tagCollector.collect(dataList);
        List<String> explicits = group.collectExplicit();
        List<String> implicits = group.collectImplicit();
        List<String> predicts = group.collectPredict();
        persist(data.getCatalystId(), explicits, implicits, predicts);

        Set<String> countingTags = new HashSet<>();
        countingTags.addAll(group.collectTrusted());
        countingTags.addAll(implicits);

        // Counting Tags
        countingTags.forEach(s -> counter.increment(s, "total"));
        if (!hasImage(dataList)) {
            countingTags.forEach(s -> counter.increment(s, "noImage"));
        }

        sleep(100);
        if (processed % 100 == 0) logger.info("Processed {}", processed);
    }

    public void persist(String placeId, List<String> explicits, List<String> implicits, List<String> predicts) {
        CorpusData data = new CorpusData(System.currentTimeMillis());
        data.put(MetaKey.version, "2018-03-08");
        data.getFields().addAll(TagKey.explicits.createFields(explicits));
        data.getFields().addAll(TagKey.implicits.createFields(implicits));
        data.getFields().addAll(TagKey.predicts.createFields(predicts));
        corpusClient.put("Sg.Munch.Place.Tag", placeId, data);
    }

    private boolean hasImage(List<CorpusData> dataList) {
        for (CorpusData data : dataList) {
            if (data.getCorpusName().equals("Sg.Munch.Place.Image")) {
                return PlaceKey.image.has(data);
            }
        }
        return false;
    }

    @Override
    protected void deleteCycle(long cycleNo) {
        counter.forEach((tag, map) -> {
            int total = map.getOrDefault("total", 0);
            int noImage = map.getOrDefault("noImage", 0);
            database.put(tag, total, noImage, 0);
            sleep(3000);
        });
        this.counter = null;
        super.deleteCycle(cycleNo);
    }
}
