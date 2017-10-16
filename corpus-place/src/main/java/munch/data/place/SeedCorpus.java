package munch.data.place;

import catalyst.utils.iterators.NestedIterator;
import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.field.PlaceKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;
import java.util.Set;

/**
 * Created By: Fuxing Loh
 * Date: 8/10/2017
 * Time: 7:39 PM
 * Project: munch-data
 */
@Singleton
public final class SeedCorpus extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(SeedCorpus.class);

    private final Set<String> seedNames;
    private final Amalgamate amalgamate;

    @Inject
    public SeedCorpus(@Named("place.seeds") Set<String> seedNames, Amalgamate amalgamate) {
        super(logger);
        this.seedNames = seedNames;
        this.amalgamate = amalgamate;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofMinutes(60);
    }

    @Override
    protected long loadCycleNo() {
        return System.currentTimeMillis();
    }

    /**
     * @param cycleNo current cycleNo
     * @return all data to seed
     */
    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return new NestedIterator<>(seedNames.iterator(),
                corpusName -> corpusClient.list(corpusName)
        );
    }

    /**
     * Maintain Each(Place)
     * 1. Validate()
     * 2. Add()
     *
     * @param cycleNo   cycleNo current cycleNo
     * @param seedData  each data to process
     * @param processed processed data count
     */
    @Override
    protected void process(long cycleNo, CorpusData seedData, long processed) {
        // Check CorpusData is valid
        if (!amalgamate.isValid(seedData)) return;
        // If Sg.Munch.Place already exist, can skip, can never have more then 1 because key is catalystId
        if (catalystClient.countCorpus(seedData.getCatalystId(), corpusName) > 0) return;

        putPlaceData(seedData);
        if (processed % 100 == 0) sleep(Duration.ofSeconds(8));
    }

    /**
     * Put created Sg.Munch.Place
     *
     * @param seedData seed data to copy from
     */
    private void putPlaceData(CorpusData seedData) {
        CorpusData placeData = new CorpusData(corpusName, System.currentTimeMillis());
        placeData.setCatalystId(seedData.getCatalystId());

        placeData.put(PlaceKey.name, PlaceKey.name.getValue(seedData));
        placeData.put(PlaceKey.Location.postal, PlaceKey.Location.postal.getValue(seedData));
        placeData.put(PlaceKey.Location.latLng, PlaceKey.Location.latLng.getValue(seedData));

        corpusClient.put(corpusName, seedData.getCatalystId(), placeData);
        logger.info("Seeded corpusName: {}, corpusKey: {} to catalystId: {}",
                seedData.getCorpusName(), seedData.getCorpusKey(), placeData.getCatalystId());
        counter.increment("Seeded");
    }
}
