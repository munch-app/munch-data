package munch.data.place.graph;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import corpus.data.CatalystClient;
import corpus.data.CorpusClient;
import corpus.data.CorpusData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
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

    @Inject
    public PlaceGraphTest(CorpusClient corpusClient, CatalystClient catalystClient, PlaceGraph placeGraph) {
        this.corpusClient = corpusClient;
        this.catalystClient = catalystClient;
        this.placeGraph = placeGraph;
    }

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

    private void graphPrintActions(String corpusName, String corpusKey) {
        CorpusData corpusData = corpusClient.get(corpusName, corpusKey);
        String placeId = corpusData.getCatalystId();

        PlaceTree tree = new PlaceTree("seed", corpusData);
        List<CorpusData> dataList = Lists.newArrayList(catalystClient.listCorpus(placeId));
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
        graphTest.graphPrintActions("Sg.Nea.TrackRecord", "585802d3b75a18ab29ba2c1d5ef2c5b8ed97be697bbf2cff47071159d0901062a53020066c9cd6796ec99dd4e46d7140d345a2159bee548314021bb8ce05e1b2");
    }
}