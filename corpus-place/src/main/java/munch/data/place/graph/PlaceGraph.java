package munch.data.place.graph;

import corpus.data.CorpusClient;
import corpus.data.CorpusData;
import munch.data.place.graph.linker.LinkerManager;
import munch.data.place.graph.matcher.MatcherManager;
import munch.data.place.graph.seeder.Seeder;
import munch.data.place.graph.seeder.SeederManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

/**
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 8:09 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceGraph {
    private static final Logger logger = LoggerFactory.getLogger(PlaceGraph.class);

    private final CorpusClient corpusClient;

    private final MatcherManager matcherManager;
    private final LinkerManager linkerManager;
    private final SeederManager seederManager;

    @Inject
    public PlaceGraph(CorpusClient corpusClient, MatcherManager matcherManager, LinkerManager linkerManager, SeederManager seederManager) {
        this.corpusClient = corpusClient;
        this.matcherManager = matcherManager;
        this.linkerManager = linkerManager;
        this.seederManager = seederManager;
    }

    /**
     * @param placeId   to search
     * @param placeTree to validate
     * @return Seeder result
     */
    public Seeder.Result search(String placeId, PlaceTree placeTree, List<CorpusData> dataList) {
        Set<CorpusData> insideSet = new HashSet<>();

        // End if failed to find seed reference
        if (!placeTree.updateReference(dataList)) return Seeder.Result.Proceed;

        // Validate existing tree, remove those that don't belong
        insideSet.add(placeTree.getCorpusData());
        for (PlaceTree right : placeTree.getTrees()) {
            validate(placeId, placeTree.getCorpusData(), right, dataList, insideSet);
        }

        List<Action> actionList = new ArrayList<>();

        // Collect data entering to tree
        Set<CorpusData> linkedSet = new HashSet<>(dataList);
        linkedSet.removeAll(insideSet);

        // Try link and remove all unlinked data
        for (CorpusData right : linkedSet) {
            if (!tryLink(placeId, placeTree, right)) {
                actionList.add(Action.unlinked(right));
            }
        }

        // Collected search result
        Set<CorpusData> searchedSet = matcherManager.search(placeTree);
        searchedSet.removeAll(linkedSet);
        searchedSet.removeAll(insideSet);

        // Try link and persist all linked data
        for (CorpusData right : searchedSet) {
            if (tryLink(placeId, placeTree, right)) {
                actionList.add(Action.linked(right));
            }
        }

        if (!actionList.isEmpty()) {
            logger.info("Applying {} Actions for PlaceGraph id: {}", actionList.size(), placeId);
            for (Action action : actionList) {
                if (action.link) {
                    corpusClient.patchCatalystId(action.data.getCorpusName(), action.data.getCorpusKey(), placeId);
                } else {
                    corpusClient.patchCatalystId(action.data.getCorpusName(), action.data.getCorpusKey(), null);
                }
            }
        }

        // Check whether it can be seeded
        return seederManager.trySeed(placeTree);
    }

    /**
     * @param left      tree to validate with, verified data
     * @param right     tree to validate against
     * @param dataList  for updating reference
     * @param insideSet to collect data in place tree
     */
    private void validate(String placeId, CorpusData left, PlaceTree right, List<CorpusData> dataList, Set<CorpusData> insideSet) {
        // Failed to find reference
        if (!right.updateReference(dataList)) return;

        Map<String, Integer> matcher = matcherManager.match(placeId, left, right.getCorpusData());
        if (linkerManager.validate(right.getLinkerName(), matcher, left, right.getCorpusData())) {
            insideSet.add(right.getCorpusData());

            // Validate all again
            for (PlaceTree innerRight : right.getTrees()) {
                validate(placeId, right.getCorpusData(), innerRight, dataList, insideSet);
            }
        }
    }

    /**
     * @param left  existing place tree
     * @param right entering corpus data
     * @return whether successfully linked
     */
    private boolean tryLink(String placeId, PlaceTree left, CorpusData right) {
        Map<String, Integer> matcher = matcherManager.match(placeId, left.getCorpusData(), right);
        Optional<String> linked = linkerManager.link(matcher, left.getCorpusData(), right);

        // Manage to find a link
        if (linked.isPresent()) {
            left.getTrees().add(new PlaceTree(linked.get(), right));
            return true;
        }

        // Nested find link (Pre-order)
        for (PlaceTree innerLeft : left.getTrees()) {
            if (tryLink(placeId, innerLeft, right)) return true;
        }

        // No link found
        return false;
    }

    /**
     * Action for CorpusData
     */
    private static class Action {
        private final boolean link;
        private final CorpusData data;

        private Action(boolean link, CorpusData data) {
            this.link = link;
            this.data = data;
        }

        public static Action linked(CorpusData data) {
            return new Action(true, data);
        }

        public static Action unlinked(CorpusData data) {
            return new Action(false, data);
        }
    }
}
