package munch.data.place.graph;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import corpus.data.CatalystClient;
import corpus.data.CorpusClient;
import corpus.data.CorpusData;
import corpus.data.DocumentClient;
import munch.restful.core.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 31/3/2018
 * Time: 2:48 AM
 * Project: munch-data
 */
@Singleton
public class PlaceGraphTest {
    private static final Logger logger = LoggerFactory.getLogger(PlaceGraphTest.class);

    private final CorpusClient corpusClient;
    private final CatalystClient catalystClient;
    private final PlaceGraph placeGraph;

    private final DocumentClient documentClient;

    @Inject
    public PlaceGraphTest(CorpusClient corpusClient, CatalystClient catalystClient, PlaceGraph placeGraph, DocumentClient documentClient) {
        this.corpusClient = corpusClient;
        this.catalystClient = catalystClient;
        this.placeGraph = placeGraph;
        this.documentClient = documentClient;
    }

    @SuppressWarnings("Duplicates")
    protected PlaceGraph.Result tryBuildTree(PlaceTree existingTree, String placeId, List<CorpusData> dataList) {
        PlaceGraph.Result result = PlaceGraph.Result.ofFailed(dataList);

        // Try build from initial place tree if exist
        if (existingTree != null) {
            result = placeGraph.search(placeId, existingTree, dataList);
        }

        List<PlaceGraph.Result> results = new ArrayList<>();

        // Only try rebuild if initial build failed
        if (result.status != PlaceGraph.Status.Seeded) {
            for (CorpusData data : dataList) {
                result = placeGraph.search(placeId, new PlaceTree(data), dataList);

                // Manage to build, exit
                if (result.status == PlaceGraph.Status.Seeded) return result;
                results.add(result);
            }
        }

        return results.stream()
                .min(Comparator.comparingInt(o -> o.actions.size()))
                .orElse(result);
    }

    private PlaceTree getPlaceTree(String placeId) {
        JsonNode node = documentClient.get(PlaceDatabase.TABLE_NAME, placeId, "0");
        if (node == null) return null;

        return JsonUtils.toObject(node, RootPlaceTree.class);
    }

    private void graphPrintActions(String corpusName, String corpusKey) {
        CorpusData corpusData = corpusClient.get(corpusName, corpusKey);
        String placeId = corpusData.getCatalystId();

        PlaceTree tree = getPlaceTree(placeId);
        List<CorpusData> dataList = Lists.newArrayList(catalystClient.listCorpus(placeId));
        dataList.removeIf(data -> data.getCorpusName().equals("Sg.Munch.Place"));

        PlaceGraph.Result result = tryBuildTree(tree, placeId, dataList);

        for (PlaceGraph.Action action : result.actions) {
            if (action.link) {
                if (placeId.equals(action.data.getCatalystId())) continue;
                logger.info("Applied LINKED for {} id: {} placeId: {}", action.data.getCorpusName(), action.data.getCorpusKey(), placeId);
            } else {
                if (!placeId.equals(action.data.getCatalystId())) continue;
                logger.info("Applied UN-LINK for {} id: {} placeId: {}", action.data.getCorpusName(), action.data.getCorpusKey(), placeId);
            }
        }
    }

    public static void main(String[] args) {
        Injector injector = PlaceGraphTestModule.getInjector();
        PlaceGraphTest graphTest = injector.getInstance(PlaceGraphTest.class);

//        graphTest.graphPrintActions("Sg.MunchSheet.PlaceInfo2", "reclmMDsqFa60Hbpq");
        // c9ce5173-e5e4-48ef-9370-c785d6895771
        // 308f291f-264c-40ee-beef-4433d1b51e55
        // 5f0967f6-72ce-4b49-a31f-fa2b73f4cd9a
        graphTest.graphPrintActions("Sg.Munch.Place", "803c83f4-2cea-45cb-915e-54f7b389e48b");
    }
}