package munch.place;

import catalyst.utils.iterators.NestedIterator;
import com.google.common.collect.ImmutableSet;
import com.typesafe.config.Config;
import corpus.data.CorpusData;
import corpus.engine.AbstractEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;
import java.util.Set;

/**
 * Created By: Fuxing Loh
 * Date: 8/10/2017
 * Time: 8:01 PM
 * Project: munch-data
 */
@Singleton
public class SyncCorpus extends AbstractEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(SyncCorpus.class);

    private final Set<String> corpusNames;

    @Inject
    public SyncCorpus(Config config) {
        super(logger);

        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        builder.addAll(config.getStringList("place.seeds"));
        builder.addAll(config.getStringList("place.trees"));
        this.corpusNames = builder.build();
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
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return new NestedIterator<>(corpusNames.iterator(),
                corpusName -> corpusClient.list(corpusName)
        );
    }

    @Override
    protected void process(long cycleNo, CorpusData data, long processed) {
        // TODO: Put into service-place-search

        if (processed % 100 == 0) {
            sleep(Duration.ofSeconds(5));
        }
    }
}
