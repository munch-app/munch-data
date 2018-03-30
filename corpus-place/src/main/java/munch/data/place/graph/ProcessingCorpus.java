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

import javax.annotation.Nullable;
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
    private final PlaceGraph placeGraph;
    private final PlaceDatabase placeDatabase;

    @Inject
    public ProcessingCorpus(Config config, CatalystClient catalystClient, ElasticClient elasticClient,
                            PlaceDatabase placeDatabase, PlaceGraph placeGraph) {
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
        String placeId = data.getCatalystId();
        PlaceTree placeTree = placeDatabase.get(placeId);
        List<CorpusData> dataList = Lists.newArrayList(catalystClient.listCorpus(placeId));

        if (placeTree != null) {
            // If PlaceTree Already exists: search and maintain
            if (!buildTree(placeId, placeTree, dataList)) {
                // Failed to build tree, try rebuilding
                placeTree = tryBuildTree(placeId, dataList);

                // Failed at rebuilding, remove from database
                if (placeTree == null) {
                    logger.info("Failed rebuilding PlaceTree: {}", placeId);
                    placeDatabase.delete(placeId);
                }
            }
        } else {
            // PlaceTree don't exists: try seed
            placeTree = tryBuildTree(placeId, dataList);
        }

        elasticClient.put(cycleNo, data, placeTree);

        // Max 30 graph per sec?
        sleep(10);
        if (processed % 1000 == 0) logger.info("Processed {} Data", processed);
    }

    /**
     * @param placeId  catalyst id
     * @param dataList list of possible tree
     * @return PlaceTree that got created
     */
    @Nullable
    private PlaceTree tryBuildTree(String placeId, List<CorpusData> dataList) {
        for (CorpusData data : dataList) {
            PlaceTree placeTree = new PlaceTree(data);
            // Return if successfully build tree
            if (buildTree(placeId, placeTree, dataList)) return placeTree;
        }

        return null;
    }

    /**
     * @param placeId  catalyst id
     * @param dataList list of possible tree
     * @return if tree is build successfully
     */
    private boolean buildTree(String placeId, PlaceTree placeTree, List<CorpusData> dataList) {
        switch (placeGraph.search(placeId, placeTree, dataList)) {
            case Proceed:
            case Block:
            default:
                return false;

            case Seed:
                placeDatabase.put(placeId, placeTree, false);
                return true;
            case Decayed:
                placeDatabase.put(placeId, placeTree, true);
                return true;
        }
    }

    @Override
    protected void deleteCycle(long cycleNo) {
        elasticClient.deleteBefore(cycleNo);
        super.deleteCycle(cycleNo);
    }
}
