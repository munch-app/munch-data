package munch.data.place;

import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;

/**
 * Created by: Fuxing
 * Date: 14/2/18
 * Time: 7:35 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceTrackingCorpus extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(PlaceTrackingCorpus.class);

    public PlaceTrackingCorpus() {
        super(logger);
    }

    @Override
    protected Duration cycleDelay() {
        // Every 12 hours generate one list
        return Duration.ofHours(12);
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return null;
    }

    @Override
    protected void process(long cycleNo, CorpusData data, long processed) {

    }
}
