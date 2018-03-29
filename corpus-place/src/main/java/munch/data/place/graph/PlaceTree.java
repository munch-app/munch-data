package munch.data.place.graph;

import com.fasterxml.jackson.annotation.JsonIgnore;
import corpus.data.CorpusData;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 8:09 PM
 * Project: munch-data
 */
public class PlaceTree {
    private String linkerName;
    private CorpusData corpusData;
    private Set<PlaceTree> trees = Set.of();

    /**
     * @param corpusData corpus data as seed
     */
    public PlaceTree(CorpusData corpusData) {
        this("seed", corpusData);
    }

    /**
     * @param linkerName linker name to use
     * @param corpusData corpus data as seed
     */
    public PlaceTree(String linkerName, CorpusData corpusData) {
        this.linkerName = linkerName;
        this.corpusData = corpusData;
    }

    public PlaceTree() {

    }

    /**
     * @return link used to establish this tree
     */
    public String getLinkerName() {
        return linkerName;
    }

    public void setLinkerName(String linkerName) {
        this.linkerName = linkerName;
    }

    /**
     * @return linked corpus data
     */
    public CorpusData getCorpusData() {
        return corpusData;
    }

    public void setCorpusData(CorpusData corpusData) {
        this.corpusData = corpusData;
    }

    /**
     * @return linked trees
     */
    public Set<PlaceTree> getTrees() {
        return trees;
    }

    public void setTrees(Set<PlaceTree> trees) {
        this.trees = trees;
    }

    @JsonIgnore
    public Set<String> getCorpusNames() {
        Set<String> names = new HashSet<>();
        findCorpusNames(names, this);
        return names;
    }

    private static void findCorpusNames(Set<String> names, PlaceTree placeTree) {
        names.add(placeTree.getCorpusData().getCorpusName());
        for (PlaceTree tree : placeTree.getTrees()) {
            findCorpusNames(names, tree);
        }
    }
}
