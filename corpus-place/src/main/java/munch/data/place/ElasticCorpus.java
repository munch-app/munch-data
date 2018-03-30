package munch.data.place;

import catalyst.utils.iterators.NestedIterator;
import com.google.common.collect.ImmutableSet;
import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.field.PlaceKey;
import munch.data.place.elastic.ElasticClient;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created By: Fuxing Loh
 * Date: 8/10/2017
 * Time: 8:01 PM
 * Project: munch-data
 */
abstract class ElasticCorpus extends CatalystEngine<CorpusData> {

    private final Set<String> names;
    private final ElasticClient elasticClient;

    public ElasticCorpus(Logger logger, Collection<String> names, ElasticClient elasticClient) {
        super(logger);
        this.names = ImmutableSet.copyOf(names);
        this.elasticClient = elasticClient;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofMinutes(80);
    }

    @Override
    protected long loadCycleNo() {
        return System.currentTimeMillis();
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return new NestedIterator<>(names.iterator(),
                corpusName -> corpusClient.list(corpusName)
        );
    }

    @Override
    protected void process(long cycleNo, CorpusData data, long processed) {
        ElasticPlace place = createPlace(data);
        if (place == null) return;

        elasticClient.put(cycleNo, place);
        counter.increment("Put");

        sleep(20);
    }

    @Override
    protected void deleteCycle(long cycleNo) {
        elasticClient.deleteBefore(cycleNo);
    }

    @Nullable
    private static ElasticPlace createPlace(CorpusData data) {
        List<String> name = PlaceKey.name.getAllValue(data);
        List<String> postal = PlaceKey.Location.postal.getAllValue(data);
        String latLng = PlaceKey.Location.latLng.getValue(data);
        if (name.isEmpty()) return null;

        ElasticPlace partial = new ElasticPlace();
        partial.setCorpusName(data.getCorpusName());
        partial.setCorpusKey(data.getCorpusKey());
        partial.setName(name);
        partial.setPostal(postal);
        partial.setLatLng(latLng);
        return partial;
    }
}
