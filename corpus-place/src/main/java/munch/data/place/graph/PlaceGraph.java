package munch.data.place.graph;

import com.google.common.collect.ImmutableList;
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
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 8:09 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceGraph {
    private static final Logger logger = LoggerFactory.getLogger(PlaceGraph.class);

    private final MatcherManager matcherManager;
    private final LinkerManager linkerManager;
    private final SeederManager seederManager;

    @Inject
    public PlaceGraph(MatcherManager matcherManager, LinkerManager linkerManager, SeederManager seederManager) {
        this.matcherManager = matcherManager;
        this.linkerManager = linkerManager;
        this.seederManager = seederManager;
    }

    /**
     * @param placeId   to search
     * @param placeTree to validate
     * @return Seeder result
     */
    public Result search(String placeId, PlaceTree placeTree, Collection<CorpusData> dataList) {
        Set<CorpusData> insideCollection = new HashSet<>();
        dataList = new HashSet<>(dataList);

        // End if failed to find seed reference
        if (!placeTree.updateReference(dataList)) return Result.ofFailed(dataList);
        insideCollection.add(placeTree.getCorpusData());

        // Validate existing tree, remove those that don't belong
        for (PlaceTree right : ImmutableList.copyOf(placeTree.getTrees())) {
            validate(placeId, placeTree, right, dataList, insideCollection);
            // Fixed: Those that failed to be validated must be removed from the tree
        }

        List<Action> actionList = new ArrayList<>();

        // Collect data entering to tree
        Set<CorpusData> toLinkCollection = new HashSet<>(dataList);
        toLinkCollection.removeAll(insideCollection);

        boolean looping;

        do {
            looping = false;
            // Try link and remove all unlinked data
            for (CorpusData right : toLinkCollection) {
                if (tryLink(placeId, placeTree, right)) {
                    toLinkCollection.remove(right);
                    insideCollection.add(right);
                    looping = true;
                    break;
                }
            }
        } while (looping);

        // Remove those failed to Link
        for (CorpusData right : toLinkCollection) {
            actionList.add(Action.of(false, right));
        }

        // Collected search result
        Set<CorpusData> searchedCollection = matcherManager.search(placeTree);
        searchedCollection.removeAll(toLinkCollection);
        searchedCollection.removeAll(insideCollection);

        // Try link and persist all linked data
        for (CorpusData right : searchedCollection) {
            if (tryLink(placeId, placeTree, right)) {
                actionList.add(Action.of(true, right));
            }
        }

        // Try seed and return result
        return Result.of(seederManager.trySeed(placeId, placeTree), placeTree, actionList);
    }

    /**
     * @param left      tree to validate with, verified data
     * @param right     tree to validate against
     * @param dataList  for updating reference
     * @param insideSet to collect data in place tree
     */
    private void validate(String placeId, PlaceTree left, PlaceTree right, Collection<CorpusData> dataList, Set<CorpusData> insideSet) {
        // Failed to find reference
        if (!right.updateReference(dataList)) {
            // Failed validation
            // Remove Right from left
            left.getTrees().remove(right);
            return;
        }

        Map<String, Integer> matcher = matcherManager.match(placeId, left.getCorpusData(), right.getCorpusData());
        if (linkerManager.validate(right.getLinkerName(), placeId, left, matcher, right.getCorpusData())) {
            insideSet.add(right.getCorpusData());

            // Validate all again
            for (PlaceTree innerRight : ImmutableList.copyOf(right.getTrees())) {
                validate(placeId, right, innerRight, dataList, insideSet);
            }
        } else {
            // Failed Validation
            // Remove Right from left
            left.getTrees().remove(right);
        }
    }

    /**
     * @param left  existing place tree
     * @param right entering corpus data
     * @return whether successfully linked
     */
    private boolean tryLink(String placeId, PlaceTree left, CorpusData right) {
        Map<String, Integer> matcher = matcherManager.match(placeId, left.getCorpusData(), right);
        Optional<String> linked = linkerManager.link(placeId, left, matcher, right);

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
     * Result of Graph Search
     */
    public static class Result {
        public final Status status;
        public final PlaceTree placeTree;
        public final List<Action> actions;

        public Result(Status status, PlaceTree placeTree, List<Action> actions) {
            this.status = status;
            this.placeTree = placeTree;
            this.actions = actions;
        }

        public static Result ofFailed(Collection<CorpusData> dataList) {
            List<Action> actions = dataList.stream()
                    .map(data -> Action.of(false, data))
                    .collect(Collectors.toList());
            return new Result(Status.Failed, null, actions);
        }

        public static Result of(Seeder.Result result, PlaceTree placeTree, List<Action> actions) {
            switch (result) {
                case Proceed:
                    return new Result(Status.Failed, placeTree, actions);
                case Block:
                    return new Result(Status.Remove, placeTree, actions);
                case Seed:
                    return new Result(Status.Seeded, placeTree, actions);
                case Decayed:
                    return new Result(Status.Decayed, placeTree, actions);

                default:
                    throw new IllegalStateException();
            }
        }

        @Override
        public String toString() {
            return "Result{" +
                    "status=" + status +
                    ", placeTree=" + placeTree +
                    ", actions=" + actions +
                    '}';
        }
    }

    public enum Status {
        Seeded, Decayed, Failed, Remove
    }

    /**
     * Action to update data in corpus
     */
    public static class Action {
        public final boolean link;
        public final CorpusData data;

        private Action(boolean link, CorpusData data) {
            this.link = link;
            this.data = data;
        }

        public static Action of(boolean link, CorpusData data) {
            return new Action(link, data);
        }

        @Override
        public String toString() {
            return "Action{" +
                    "link=" + link +
                    ", data=" + data +
                    '}';
        }
    }
}
