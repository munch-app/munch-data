package munch.place;

import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.exception.NotFoundException;
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
public class TreeCorpus extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(TreeCorpus.class);

    private final Amalgamate amalgamate;

    @Inject
    public TreeCorpus(Amalgamate amalgamate) {
        super(logger);
        this.amalgamate = amalgamate;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofMinutes(15);
    }

    @Override
    protected long loadCycleNo() {
        return System.currentTimeMillis();
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
     * @param placeData each data to process
     * @param processed processed data count
     */
    @Override
    protected void process(long cycleNo, CorpusData placeData, long processed) {
        try {
            if (amalgamate.maintain(placeData)) {
                // TODO Update Sg.Munch.Place
                // TODO Client.Put if hash change
            } else {
                // TODO Client.Remove
            }

            // Sleep for 1 second every 5 processed
            if (processed % 5 == 0) {
                sleep(1000);
            }
        } catch (NotFoundException e) {
            logger.warn("Amalgamate Conflict Error catalystId: {}", placeData.getCatalystId(), e);
        }
    }
}
