package munch.data.place;

import catalyst.utils.iterators.NestedIterator;
import corpus.data.CorpusData;
import munch.data.place.graph.PlaceGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 8:07 PM
 * Project: munch-data
 */
@Singleton
public final class IndexCorpus extends AbstractCorpus {
    private static final Logger logger = LoggerFactory.getLogger(IndexCorpus.class);

    @Inject
    public IndexCorpus() {
        super(logger);
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return new NestedIterator<>(corpusNameList.iterator(),
                corpusName -> corpusClient.list(corpusName)
        );
    }

    @Override
    protected void process(long cycleNo, String placeId, CorpusData data) {
        // If contains Sg.Munch.Place, PlaceCorpus will do the indexing
        if (catalystClient.hasCorpus(placeId, "Sg.Munch.Place")) return;

        PlaceGraph.Result result = tryBuildTree(null, placeId, List.of(data));
        switch (result.status) {
            case Seeded:
                applyActions(placeId, result.actions);
                placeDatabase.putTree(placeId, result.placeTree, false);
                index(result.placeTree);
                return;

            case Decayed:
                applyActions(placeId, result.actions);
                placeDatabase.putTree(placeId, result.placeTree, true);
                index(result.placeTree);
                return;

            case Failed:
                index(data, null);
                return;

            default:
                throw new IllegalStateException();
        }
    }

    @Override
    protected void deleteCycle(long cycleNo) {
        // Remove data that are 4 days old
        long before = System.currentTimeMillis() - Duration.ofDays(4).toMillis();
        elasticClient.deleteBefore(before);
        super.deleteCycle(cycleNo);
    }
}
