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



    private void graphPrintActions(String corpusName, String corpusKey) {
        CorpusData corpusData = corpusClient.get(corpusName, corpusKey);
        String placeId = corpusData.getCatalystId();

        PlaceTree tree = new PlaceTree("seed", corpusData);
        PlaceGraph.Result result = placeGraph.search(placeId, tree, Lists.newArrayList(catalystClient.listCorpus(placeId)));

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

        graphTest.graphPrintActions("Sg.MunchSheet.PlaceInfo2", "reclmMDsqFa60Hbpq");
    }
}