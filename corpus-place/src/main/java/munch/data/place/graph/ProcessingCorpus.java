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
        RootPlaceTree rootTree = placeDatabase.get(placeId);

        PlaceTree placeTree = rootTree != null ? rootTree.getTree() : null;

        // Skip if already done by current cycle
        if (rootTree == null || rootTree.getCycleNo() != cycleNo) {
            List<CorpusData> dataList = Lists.newArrayList(catalystClient.listCorpus(placeId));
            if (placeTree != null) {
                // If PlaceTree Already exists: search and maintain
                placeTree = tryBuildTree(cycleNo, placeTree, placeId, dataList);
            } else {
                // PlaceTree don't exists: try build tree
                placeTree = tryBuildTree(cycleNo, null, placeId, dataList);
            }
        }

        elasticClient.put(cycleNo, data, placeTree);

        if (processed % 1000 == 0) logger.info("Processed {} Data", processed);
    }

    /**
     * @param placeId  catalyst id
     * @param dataList list of possible tree
     * @return PlaceTree that got created
     */
    @Nullable
    private PlaceTree tryBuildTree(long cycleNo, PlaceTree existingTree, String placeId, List<CorpusData> dataList) {
        PlaceGraph.Result result = PlaceGraph.Result.ofFailed(dataList);

        // Try build from initial place tree if exist
        if (existingTree != null) {
            result = placeGraph.search(placeId, existingTree, dataList);
        }

        // Only try rebuild if initial build failed
        if (result.status == PlaceGraph.Status.Failed) {
            for (CorpusData data : dataList) {
                result = placeGraph.search(placeId, new PlaceTree(data), dataList);

                // Manage to build, exit
                if (result.status != PlaceGraph.Status.Failed) break;
            }
        }

        // After building, persist if seeded or decayed
        switch (result.status) {
            case Seeded:
                applyActions(placeId, result.actions);
                placeDatabase.put(placeId, cycleNo, result.placeTree, false);
                return result.placeTree;
            case Decayed:
                applyActions(placeId, result.actions);
                placeDatabase.put(placeId, cycleNo, result.placeTree, true);
                return result.placeTree;
            case Failed:
                // If Failed, only delete if existing tree exist
                if (existingTree != null) {
                    applyActions(placeId, result.actions);
                    placeDatabase.delete(placeId);
                    logger.info("Failed rebuilding PlaceTree: {}", placeId);
                }
                return null;
            default:
                throw new IllegalStateException();
        }
    }

    private void applyActions(String placeId, List<PlaceGraph.Action> actionList) {
        if (actionList.isEmpty()) return;

        for (PlaceGraph.Action action : actionList) {
            if (action.link) {
                if (placeId.equals(action.data.getCatalystId())) continue;

                corpusClient.patchCatalystId(action.data.getCorpusName(), action.data.getCorpusKey(), placeId);
                logger.info("Applied LINKED for {} id: {} placeId: {}", action.data.getCorpusName(), action.data.getCorpusKey(), placeId);
            } else {
                if (!placeId.equals(action.data.getCatalystId())) continue;

                corpusClient.patchCatalystId(action.data.getCorpusName(), action.data.getCorpusKey(), null);
                logger.info("Applied UN-LINK for {} id: {} placeId: {}", action.data.getCorpusName(), action.data.getCorpusKey(), placeId);
            }
        }
    }

    @Override
    protected void deleteCycle(long cycleNo) {
        elasticClient.deleteBefore(cycleNo);
        super.deleteCycle(cycleNo);
    }
}
