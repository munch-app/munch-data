package munch.data.place.graph;

import catalyst.utils.iterators.NestedIterator;
import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import corpus.data.CatalystClient;
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

    private final CatalystClient catalystClient;

    private final ElasticClient elasticClient;
    private final PlaceDatabase placeDatabase;
    private final PlaceGraph placeGraph;

    @Inject
    public ProcessingCorpus(Config config, CatalystClient catalystClient, ElasticClient elasticClient, PlaceDatabase placeDatabase, PlaceGraph placeGraph) {
        super(logger);
        this.corpus = config.getStringList("graph.corpus");
        this.catalystClient = catalystClient;
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
        List<CorpusData> dataList = Lists.newArrayList(catalystClient.listCorpus(placeId));

        if (placeTree != null) {
            // If PlaceTree Already exists: search and maintain
            placeTree = placeGraph.search(placeId, placeTree, dataList);

            // Persist tree updates
            if (placeTree != null) placeDatabase.put(placeTree);
            else {
                // PlaceTree corrupted, try rebuilding
                placeTree = buildTree(placeId, dataList);
                if (placeTree != null) placeDatabase.put(placeTree);
                else placeDatabase.delete(placeId);
            }
        } else {
            // PlaceTree don't exists: try seed
            // Put if seeded
            placeTree = buildTree(placeId, dataList);
            if (placeTree != null) placeDatabase.put(placeTree);
        }

        // Max 30 graph per sec?
        sleep(10);
    }

    /**
     * @param placeId  catalyst id
     * @param dataList list of possible tree
     * @return PlaceTree if found
     */
    private PlaceTree buildTree(String placeId, List<CorpusData> dataList) {
        for (CorpusData data : dataList) {
            PlaceTree placeTree = placeGraph.search(placeId, new PlaceTree(data), dataList);
            if (placeTree != null) return placeTree;
        }
        return null;
    }

    @Override
    protected void deleteCycle(long cycleNo) {
        elasticClient.deleteBefore(cycleNo);
        super.deleteCycle(cycleNo);
    }
}
