package munch.data.place;

import corpus.data.CorpusData;
import corpus.engine.CorpusEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;

/**
 * Created by: Fuxing
 * Date: 9/3/18
 * Time: 10:23 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceSuggestCorpus extends CorpusEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(PlaceSuggestCorpus.class);


    public PlaceSuggestCorpus() {
        super(logger);
    }

    @Override
    protected Duration cycleDelay() {
        return null;
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return null;
    }
}
