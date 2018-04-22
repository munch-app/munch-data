package munch.data.place;

import com.google.common.collect.Lists;
import corpus.data.CorpusData;
import munch.data.place.graph.PlaceGraph;
import munch.data.place.graph.RootPlaceTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 11/4/2018
 * Time: 11:17 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceCorpus extends AbstractCorpus {
    private static final Logger logger = LoggerFactory.getLogger(PlaceCorpus.class);

    @Inject
    public PlaceCorpus() {
        super(logger);
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return corpusClient.list("Sg.Munch.Place");
    }

    @Override
    protected void process(long cycleNo, String placeId, CorpusData data) {
        RootPlaceTree placeTree = placeDatabase.get(placeId);

        List<CorpusData> dataList = Lists.newArrayList(catalystClient.listCorpus(placeId));
        purge(dataList);

        PlaceGraph.Result result = tryBuildTree(placeTree, placeId, dataList);
        switch (result.status) {
            case Seeded:
                applyActions(placeId, result.actions);
                placeDatabase.put(placeId, result.placeTree, false);
                index(result.placeTree);
                return;

            case Decayed:
                applyActions(placeId, result.actions);
                placeDatabase.put(placeId, result.placeTree, true);
                index(result.placeTree);
                return;

            case Failed:
                applyActions(placeId, result.actions);
                placeDatabase.delete(placeId);
                logger.info("Failed rebuilding PlaceTree: {}", placeId);
                return;

            case Remove:
                applyActions(placeId, result.actions);
                placeDatabase.remove(placeId, placeTree);
                index(result.placeTree);
                return;

            default:
                throw new IllegalStateException();
        }
    }

    /**
     * @param dataList to validate, if failed will be deleted
     */
    private void purge(List<CorpusData> dataList) {
        dataList.removeIf(data -> {
            if (data.getCorpusName().equals("Sg.Munch.Place")) {
                if (!data.getCorpusKey().equals(data.getCatalystId())) {
                    corpusClient.delete(data.getCorpusName(), data.getCorpusKey());
                }
                return true;
            }
            return false;
        });
    }
}
