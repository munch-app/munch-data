package munch.data.place;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.typesafe.config.Config;
import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.exception.NotFoundException;
import munch.data.place.elastic.ElasticClient;
import munch.data.place.graph.PlaceDatabase;
import munch.data.place.graph.PlaceGraph;
import munch.data.place.graph.PlaceTree;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 12/4/2018
 * Time: 12:29 AM
 * Project: munch-data
 */
public abstract class AbstractCorpus extends CatalystEngine<CorpusData> {

    protected ElasticClient elasticClient;
    protected PlaceGraph placeGraph;
    protected PlaceDatabase placeDatabase;

    protected List<String> corpusNameList;
    protected Set<String> corpusNameSet;

    /**
     * @param logger logger for the engine
     */
    protected AbstractCorpus(Logger logger) {
        super(logger);
    }

    @Inject
    void injectRequired(Config config, ElasticClient elasticClient, PlaceGraph placeGraph, PlaceDatabase placeDatabase) {
        this.elasticClient = elasticClient;
        this.placeGraph = placeGraph;
        this.placeDatabase = placeDatabase;

        this.corpusNameList = ImmutableList.copyOf(config.getStringList("graph.corpus"));
        this.corpusNameSet = ImmutableSet.copyOf(corpusNameList);
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(1);
    }

    @Override
    protected void process(long cycleNo, CorpusData data, long processed) {
        String placeId = data.getCatalystId();
        process(cycleNo, placeId, data);

        if (processed % 1000 == 0) logger.info("Processed {} Data", processed);
    }

    protected abstract void process(long cycleNo, String placeId, CorpusData data);

    // TODO Verify index method place tree counter works as expected

    protected void index(PlaceTree placeTree) {
        for (CorpusData data : placeTree.getCorpusDataList()) {
            index(data, placeTree);
        }
    }

    protected void index(List<CorpusData> dataList, PlaceTree placeTree) {
        for (CorpusData data : dataList) {
            index(data, placeTree);
        }
    }

    protected void index(CorpusData data, PlaceTree placeTree) {
        if (corpusNameSet.contains(data.getCorpusName())) {
            elasticClient.put(System.currentTimeMillis(), data, placeTree);
        }
    }

    /**
     * @param placeId  catalyst id
     * @param dataList list of possible tree
     * @return Status of build tree
     */
    protected PlaceGraph.Result tryBuildTree(PlaceTree existingTree, String placeId, List<CorpusData> dataList) {
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

        return result;
    }

    protected void applyActions(String placeId, List<PlaceGraph.Action> actionList) {
        if (actionList.isEmpty()) return;

        for (PlaceGraph.Action action : actionList) {
            try {
                if (action.link) {
                    if (placeId.equals(action.data.getCatalystId())) continue;

                    corpusClient.patchCatalystId(action.data.getCorpusName(), action.data.getCorpusKey(), placeId);
                    logger.info("Applied LINKED for {} id: {} placeId: {}", action.data.getCorpusName(), action.data.getCorpusKey(), placeId);
                } else {
                    if (!placeId.equals(action.data.getCatalystId())) continue;

                    corpusClient.patchCatalystId(action.data.getCorpusName(), action.data.getCorpusKey(), null);
                    logger.info("Applied UN-LINK for {} id: {} placeId: {}", action.data.getCorpusName(), action.data.getCorpusKey(), placeId);
                }
            } catch (NotFoundException e) {
                counter.increment("NotFound");
            }
        }
    }
}
