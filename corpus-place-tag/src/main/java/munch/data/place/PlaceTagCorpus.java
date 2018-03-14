package munch.data.place;

import com.google.common.collect.Lists;
import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.field.MetaKey;
import corpus.field.PlaceKey;
import munch.data.clients.PlaceClient;
import munch.data.place.collector.TagCollector;
import munch.data.place.collector.TimingTagCollector;
import munch.data.place.group.PlaceTagCounter;
import munch.data.place.group.PlaceTagDatabase;
import munch.data.structure.Place;
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
    private final PlaceClient placeClient;
    private final TimingTagCollector timingTagCollector;

    private PlaceTagCounter counter;

    @Inject
    public PlaceTagCorpus(TagCollector tagCollector, PlaceTagDatabase database, PlaceClient placeClient, TimingTagCollector timingTagCollector) {
        super(logger);
        this.tagCollector = tagCollector;
        this.database = database;
        this.placeClient = placeClient;
        this.timingTagCollector = timingTagCollector;
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
        String placeId = data.getCorpusKey();
        List<CorpusData> dataList = Lists.newArrayList(catalystClient.listCorpus(data.getCatalystId()));

        TagCollector.Group group = tagCollector.collect(placeId, dataList);
        List<String> explicits = group.collectExplicit();
        List<String> implicits = group.collectImplicit();
        List<String> predicts = group.collectPredicted();
        persist(data.getCatalystId(), explicits, implicits, predicts);

        Set<String> countingTags = new HashSet<>();
        countingTags.addAll(group.collectTrusted());
        countingTags.addAll(implicits);

        // Counting Tags
        countingTags.forEach(s -> counter.increment(s, "total"));
        if (!hasImage(dataList)) {
            countingTags.forEach(s -> counter.increment(s, "noImage"));
        }

        sleep(300);
        if (processed % 100 == 0) logger.info("Processed {}", processed);
    }

    public void persist(String placeId, List<String> explicits, List<String> implicits, List<String> predicts) {
        CorpusData data = new CorpusData(System.currentTimeMillis());
        data.setCatalystId(placeId);
        data.put(MetaKey.version, "2018-03-08");
        data.getFields().addAll(TagKey.explicits.createFields(explicits));

        implicits = new ArrayList<>(implicits);
        // Collect timing tags
        for (String timing : collectTimings(placeId)) {
            if (!implicits.contains(timing)) {
                implicits.add(timing);
            }
        }

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

    private Set<String> collectTimings(String placeId) {
        Place place = placeClient.get(placeId);
        if (place == null) return Set.of();

        return timingTagCollector.get(place);
    }

    @Override
    protected void deleteCycle(long cycleNo) {
        counter.forEach((tag, map) -> {
            int total = map.getOrDefault("total", 0);
            int noImage = map.getOrDefault("noImage", 0);
            database.put(tag, total, noImage, 0, 0);
            sleep(3000);
        });
        this.counter = null;
        super.deleteCycle(cycleNo);
    }
}
