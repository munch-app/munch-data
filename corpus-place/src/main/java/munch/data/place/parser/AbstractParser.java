package munch.data.place.parser;

import com.google.common.collect.ImmutableList;
import com.typesafe.config.Config;
import corpus.data.CorpusData;
import corpus.field.AbstractKey;
import corpus.utils.FieldCollector;
import munch.data.structure.Place;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 11:56 PM
 * Project: munch-data
 */
public abstract class AbstractParser<T> {
    protected List<String> priorityCorpus;

    @Inject
    void inject(Config config) {
        this.priorityCorpus = ImmutableList.copyOf(config.getStringList("place.priority"));
    }

    /**
     * @param place read-only place data
     * @param list  list of corpus data to parse from
     * @return parsed data
     */
    public abstract T parse(Place place, List<CorpusData> list);

    /**
     * Note this method will call collectMax with priorityCorpus
     *
     * @param list list of corpus data
     * @param keys keys to collect and get max
     * @return value that appear the most
     * @see AbstractParser#collectMax(List, List, AbstractKey...)
     */
    @Nullable
    protected String collectMax(List<CorpusData> list, AbstractKey... keys) {
        return collectMax(list, priorityCorpus, keys);
    }

    @Nullable
    protected String collectMax(List<CorpusData> list, Function<String, String> mapper, AbstractKey... keys) {
        FieldCollector fieldCollector = new FieldCollector(keys);
        fieldCollector.addAll(list);
        return mapper.apply(fieldCollector.collectMax());
    }

    @Nullable
    protected String collectMax(List<CorpusData> list, List<String> corpusNames, AbstractKey... keys) {
        FieldCollector fieldCollector = new FieldCollector(keys);
        fieldCollector.addAll(list);
        String max = fieldCollector.collectMax(corpusNames);
        if (max != null) return max;
        return fieldCollector.collectMax();
    }

    @Nullable
    protected String collectMax(List<CorpusData> list, List<String> corpusNames, Function<String, String> mapper, AbstractKey... keys) {
        return mapper.apply(collectMax(list, corpusNames, keys));
    }

    protected List<String> collectValue(List<CorpusData> list, AbstractKey... keys) {
        FieldCollector fieldCollector = new FieldCollector(keys);
        fieldCollector.addAll(list);
        return fieldCollector.collect();
    }

    protected List<CorpusData.Field> collect(List<CorpusData> list, AbstractKey... keys) {
        FieldCollector fieldCollector = new FieldCollector(keys);
        fieldCollector.addAll(list);
        return fieldCollector.collectField();
    }

    protected List<CorpusData.Field> collect(CorpusData data, AbstractKey... keys) {
        FieldCollector fieldCollector = new FieldCollector(keys);
        fieldCollector.add(data);
        return fieldCollector.collectField();
    }

    protected boolean hasAny(AbstractKey key, List<CorpusData> list) {
        for (CorpusData data : list) {
            if (key.has(data)) return true;
        }
        return false;
    }

    /**
     * @param list       list of corpus data to search
     * @param corpusName corpus name to get
     * @return return matched corpus data or null
     */
    protected CorpusData find(List<CorpusData> list, String corpusName) {
        for (CorpusData corpusData : list) {
            if (corpusData.getCorpusName().equals(corpusName)) {
                return corpusData;
            }
        }
        return null;
    }

    /**
     * @param list       list of corpus data to search
     * @param corpusName corpus name to get
     * @return return all matched corpus data
     */
    protected List<CorpusData> findAll(List<CorpusData> list, String corpusName) {
        return list.stream()
                .filter(data -> data.getCorpusName().equals(corpusName))
                .collect(Collectors.toList());
    }
}
