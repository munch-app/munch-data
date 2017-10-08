package munch.place;

import corpus.data.CorpusData;
import corpus.engine.AbstractEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;

/**
 * Created By: Fuxing Loh
 * Date: 8/10/2017
 * Time: 7:39 PM
 * Project: munch-data
 */
@Singleton
public class PlaceCorpus extends AbstractEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(PlaceCorpus.class);

    @Inject
    public PlaceCorpus() {
        super(logger);
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofMinutes(30);
    }

    @Override
    protected long loadCycleNo() {
        return System.currentTimeMillis();
    }

    @Override
    protected boolean preCycle(long cycleNo) {
        /*
        Create each from [NEA]
        - Validate()
        - Add()
         */
        return super.preCycle(cycleNo);
    }

    /**
     * @param cycleNo current cycleNo
     * @return all data to maintain
     */
    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return corpusClient.list(corpusName);
    }

    /**
     * Maintain Each(Place)
     * 1. Validate()
     * 2. Add()
     *
     * @param cycleNo   cycleNo current cycleNo
     * @param data      each data to process
     * @param processed processed data count
     */
    @Override
    protected void process(long cycleNo, CorpusData data, long processed) {

    }

    // TODO: Sync Bridge Corpus
    // TODO: Maintain Each
    // TODO: - Add()
    // TODO: - Validate()
    // TODO: Search -> Merge(left, right)
}
