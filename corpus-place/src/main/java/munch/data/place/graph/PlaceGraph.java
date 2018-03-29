package munch.data.place.graph;

import com.google.common.collect.Lists;
import corpus.data.CatalystClient;
import corpus.data.CorpusData;
import munch.data.place.graph.linker.LinkerManager;
import munch.data.place.graph.matcher.MatcherManager;
import munch.data.place.graph.seeder.SeederManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 8:09 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceGraph {
    private static final Logger logger = LoggerFactory.getLogger(PlaceGraph.class);

    private final CatalystClient catalystClient;

    private final MatcherManager matcherManager;
    private final LinkerManager linkerManager;
    private final SeederManager seederManager;

    @Inject
    public PlaceGraph(CatalystClient catalystClient, MatcherManager matcherManager, LinkerManager linkerManager, SeederManager seederManager) {
        this.catalystClient = catalystClient;
        this.matcherManager = matcherManager;
        this.linkerManager = linkerManager;
        this.seederManager = seederManager;
    }

    /**
     * @param placeId   to search
     * @param placeTree to validate
     * @return PlaceTree if can be seeded, or null
     */
    @Nullable
    public PlaceTree search(String placeId, PlaceTree placeTree) {
        List<CorpusData> dataList = Lists.newArrayList(catalystClient.listCorpus(placeId));

        // Validate existing tree, remove those that don't belong
        validate(placeTree, dataList);

        // Look for new links
        List<CorpusData> searchedList = search(placeTree);

        if (seederManager.trySeed(placeTree)) return placeTree;
        return null;
    }

    private void validate(PlaceTree placeTree, List<CorpusData> list) {
        // Update Mapping
        // TODO

        // Delete or Remap
    }

    private boolean validate(String linkerName, CorpusData left, CorpusData right) {
        Map<String, Integer> matcher = matcherManager.match(left, right);
        return linkerManager.validate(linkerName, matcher);
    }

    private List<CorpusData> search(PlaceTree placeTree) {
        return null;
    }
}
