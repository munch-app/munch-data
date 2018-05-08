package munch.data.place.graph;

import com.fasterxml.jackson.annotation.JsonIgnore;
import corpus.data.CorpusData;
import corpus.field.AbstractKey;
import corpus.field.FieldUtils;
import corpus.utils.FieldCollector;

import java.util.*;
import java.util.function.Predicate;

/**
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 8:09 PM
 * Project: munch-data
 */
public class PlaceTree {
    private String linkerName;
    private CorpusData corpusData;
    private Set<PlaceTree> trees = new HashSet<>();

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

    /**
     * @return size if place tree
     */
    @JsonIgnore
    public int getSize() {
        int size = 1;
        for (PlaceTree tree : trees) {
            size += tree.getSize();
        }

        return size;
    }

    @JsonIgnore
    public Set<String> getCorpusNames() {
        Set<String> names = new HashSet<>();
        findCorpusNames(names, this);
        return names;
    }

    /**
     * @param dataList data list from corpus to update reference
     * @return whether reference is updated
     */
    @JsonIgnore
    public boolean updateReference(Collection<CorpusData> dataList) {
        for (CorpusData data : dataList) {
            if (data.getCorpusName().equals(corpusData.getCorpusName()) &&
                    data.getCorpusKey().equals(corpusData.getCorpusKey())) {
                setCorpusData(data);
                return true;
            }
        }
        return false;
    }

    @JsonIgnore
    public List<CorpusData> getCorpusDataList() {
        List<CorpusData> list = new ArrayList<>();
        findCorpusDataList(list, this);
        return list;
    }

    @JsonIgnore
    public List<CorpusData> getCorpusDataList(String corpusName) {
        List<CorpusData> list = new ArrayList<>();
        findCorpusDataList(list, this, corpusName);
        return list;
    }

    @JsonIgnore
    public Map<String, List<CorpusData.Field>> getFieldsMap() {
        Map<String, List<CorpusData.Field>> fieldMap = new HashMap<>();
        findFields(fieldMap, this);
        return fieldMap;
    }

    @JsonIgnore
    public List<CorpusData.Field> getFields(AbstractKey key) {
        List<CorpusData.Field> fields = new ArrayList<>();
        findFields(fields, key.getKey(), this);
        return fields;
    }

    public FieldCollector getFieldCollector(AbstractKey key) {
        FieldCollector fieldCollector = new FieldCollector(key);
        collectFields(fieldCollector, this);
        return fieldCollector;
    }

    @JsonIgnore
    public boolean predicateFirstField(Predicate<CorpusData.Field> predicate) {
        return predicateFirstField(predicate, this);
    }

    private static void findCorpusNames(Set<String> names, PlaceTree placeTree) {
        names.add(placeTree.getCorpusData().getCorpusName());
        for (PlaceTree tree : placeTree.getTrees()) {
            findCorpusNames(names, tree);
        }
    }

    private static void findFields(List<CorpusData.Field> fields, String key, PlaceTree placeTree) {
        fields.addAll(FieldUtils.getAll(placeTree.getCorpusData(), key));
        for (PlaceTree tree : placeTree.getTrees()) {
            findFields(fields, key, tree);
        }
    }

    private static void findFields(Map<String, List<CorpusData.Field>> fieldMap, PlaceTree placeTree) {
        for (CorpusData.Field field : placeTree.getCorpusData().getFields()) {
            fieldMap.compute(field.getKey(), (s, fields) -> {
                if (fields == null) fields = new ArrayList<>();
                fields.add(field);
                return fields;
            });
        }

        for (PlaceTree tree : placeTree.getTrees()) {
            findFields(fieldMap, tree);
        }
    }

    private static boolean predicateFirstField(Predicate<CorpusData.Field> predicate, PlaceTree placeTree) {
        for (CorpusData.Field field : placeTree.getCorpusData().getFields()) {
            if (predicate.test(field)) return true;
        }

        for (PlaceTree tree : placeTree.getTrees()) {
            if (predicateFirstField(predicate, tree)) return true;
        }
        return false;
    }

    private static void findCorpusDataList(List<CorpusData> list, PlaceTree placeTree) {
        list.add(placeTree.getCorpusData());

        for (PlaceTree tree : placeTree.getTrees()) {
            findCorpusDataList(list, tree);
        }
    }

    private static void collectFields(FieldCollector fieldCollector, PlaceTree placeTree) {
        fieldCollector.add(placeTree.getCorpusData());

        for (PlaceTree tree : placeTree.getTrees()) {
            collectFields(fieldCollector, tree);
        }
    }

    private static void findCorpusDataList(List<CorpusData> list, PlaceTree placeTree, String corpusName) {
        if (placeTree.getCorpusData().getCorpusName().equals(corpusName)) {
            list.add(placeTree.getCorpusData());
        }

        for (PlaceTree tree : placeTree.getTrees()) {
            findCorpusDataList(list, placeTree, corpusName);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaceTree tree = (PlaceTree) o;
        return Objects.equals(linkerName, tree.linkerName) &&
                Objects.equals(corpusData, tree.corpusData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(linkerName, corpusData);
    }
}
