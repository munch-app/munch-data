package munch.place;

import catalyst.utils.iterators.NestedIterator;
import com.google.common.collect.ImmutableSet;
import com.typesafe.config.Config;
import corpus.data.CorpusData;
import corpus.engine.AbstractEngine;
import corpus.field.PlaceKey;
import munch.place.elastic.ElasticClient;
import munch.place.elastic.PartialPlace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
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
    private final ElasticClient elasticClient;

    @Inject
    public SyncCorpus(Config config, ElasticClient elasticClient) {
        super(logger);
        this.elasticClient = elasticClient;

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
        PartialPlace partial = createPartial(data);
        if (partial == null) return;

        // TODO: Don't put if already merged?

        elasticClient.put(cycleNo, partial);
        if (processed % 100 == 0) {
            sleep(Duration.ofSeconds(6));
        }
    }

    @Override
    protected void deleteCycle(long cycleNo) {
        elasticClient.delete(cycleNo);
    }

    @Nullable
    private static PartialPlace createPartial(CorpusData data) {
        List<String> name = PlaceKey.name.getAllValue(data);
        List<String> postal = PlaceKey.Location.postal.getAllValue(data);
        if (name.isEmpty() || postal.isEmpty()) return null;

        PartialPlace partial = new PartialPlace();
        partial.setCorpusName(data.getCorpusName());
        partial.setCorpusKey(data.getCorpusKey());
        partial.setName(name);
        partial.setPostal(postal);
        return partial;
    }
}
