package munch.data.container;

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
public final class SeedingCorpus extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(SeedingCorpus.class);

    @Inject
    public SeedingCorpus() {
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
        return corpusClient.list("Sg.MunchSheet.Container");
    }

    @Override
    protected void process(long cycleNo, CorpusData seedData, long processed) {
        // If Sg.Munch.Container already exist, can skip
        if (catalystClient.hasCorpus(seedData.getCatalystId(), "Sg.Munch.Container")) return;

        // Put created Sg.Munch.Location
        CorpusData data = new CorpusData(cycleNo);
        data.setCatalystId(seedData.getCatalystId());
        data.put(MunchContainerKey.updatedDate, "0");
        data.put(MunchContainerKey.sourceCorpusName, seedData.getCorpusName());

        corpusClient.put("Sg.Munch.Container", seedData.getCorpusKey(), data);
        counter.increment("Seeded");
        sleep(50);
    }
}
