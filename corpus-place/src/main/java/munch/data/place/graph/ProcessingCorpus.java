package munch.data.place.graph;

import catalyst.utils.iterators.NestedIterator;
import com.typesafe.config.Config;
import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import munch.data.place.elastic.ElasticClient;
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
public final class ProcessingCorpus extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(ProcessingCorpus.class);

    private final List<String> corpus;

    private final ElasticClient elasticClient;
    private final PlaceDatabase placeDatabase;
    private final PlaceGraph placeGraph;

    @Inject
    public ProcessingCorpus(Config config, ElasticClient elasticClient, PlaceDatabase placeDatabase, PlaceGraph placeGraph) {
        super(logger);
        this.corpus = config.getStringList("graph.corpus");
        this.elasticClient = elasticClient;
        this.placeDatabase = placeDatabase;
        this.placeGraph = placeGraph;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(1);
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return new NestedIterator<>(corpus.iterator(),
                corpusName -> corpusClient.list(corpusName)
        );
    }

    @Override
    protected void process(long cycleNo, CorpusData data, long processed) {
        elasticClient.put(cycleNo, data);

        String placeId = data.getCatalystId();
        PlaceTree placeTree = placeDatabase.get(placeId);

        if (placeTree != null) {
            // If PlaceTree Already exists: search and maintain
            placeTree = placeGraph.search(placeId, placeTree);

            // Delegate result
            if (placeTree != null) placeDatabase.put(placeTree);
            else placeDatabase.delete(placeId);
        } else {
            // PlaceTree don't exists: try seed
            placeTree = placeGraph.search(placeId, new PlaceTree(data));

            // Put if seeded
            if (placeTree != null) placeDatabase.put(placeTree);
        }

        // Max 30 graph per sec?
        sleep(10);
    }

    @Override
    protected void deleteCycle(long cycleNo) {
        elasticClient.deleteBefore(cycleNo);
        super.deleteCycle(cycleNo);
    }
}
