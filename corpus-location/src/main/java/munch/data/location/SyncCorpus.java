package munch.data.location;

import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;

/**
 * Created By: Fuxing Loh
 * Date: 8/10/2017
 * Time: 8:01 PM
 * Project: munch-data
 */
@Singleton
public class SyncCorpus extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(SyncCorpus.class);

    @Inject
    public SyncCorpus() {
        super(logger);
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofMinutes(60);
    }

    @Override
    protected long loadCycleNo() {
        return System.currentTimeMillis();
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return corpusClient.list("Sg.MunchSheet.LocationPolygon");
    }

    @Override
    protected void process(long cycleNo, CorpusData seedData, long processed) {
        // If Sg.Munch.Location already exist, can skip, can never have more then 1 because key is catalystId
        if (catalystClient.countCorpus(seedData.getCatalystId(), corpusName) > 0) return;

        // Put created Sg.Munch.Location
        CorpusData data = new CorpusData(cycleNo);
        data.setCatalystId(seedData.getCatalystId());
        data.put(LocationKey.updatedDate, "0");
        corpusClient.put(corpusName, seedData.getCorpusKey(), data);
        counter.increment("Seeded");
    }
}
