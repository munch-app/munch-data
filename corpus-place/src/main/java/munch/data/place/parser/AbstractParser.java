package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.AbstractKey;
import corpus.utils.FieldCollector;
import munch.data.structure.Place;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 11:56 PM
 * Project: munch-data
 */
public abstract class AbstractParser<T> {

    /**
     * @param place read-only place data
     * @param list  list of corpus data to parse from
     * @return parsed data
     */
    public abstract T parse(Place place, List<CorpusData> list);

    @Nullable
    protected String collectMax(List<CorpusData> list, AbstractKey... keys) {
        FieldCollector fieldCollector = new FieldCollector(keys);
        fieldCollector.addAll(list);
        return fieldCollector.collectMax();
    }

    @Nullable
    protected String collectMax(List<CorpusData> list, Function<String, String> mapper, AbstractKey... keys) {
        FieldCollector fieldCollector = new FieldCollector(keys);
        fieldCollector.addAll(list);
        return mapper.apply(fieldCollector.collectMax());
    }

    @Nullable
    protected String collectMax(List<CorpusData> list, String[] corpusNames, AbstractKey... keys) {
        FieldCollector fieldCollector = new FieldCollector(keys);
        fieldCollector.addAll(list);
        return fieldCollector.collectMax(corpusNames);
    }

    @Nullable
    protected String collectMax(List<CorpusData> list, List<String> corpusNames, AbstractKey... keys) {
        FieldCollector fieldCollector = new FieldCollector(keys);
        fieldCollector.addAll(list);
        return fieldCollector.collectMax(corpusNames);
    }

    @Nullable
    protected String collectMax(List<CorpusData> list, String[] corpusNames, Function<String, String> mapper, AbstractKey... keys) {
        FieldCollector fieldCollector = new FieldCollector(keys);
        fieldCollector.addAll(list);
        return mapper.apply(fieldCollector.collectMax(corpusNames));
    }

    @Nullable
    protected String collectMax(List<CorpusData> list, List<String> corpusNames, Function<String, String> mapper, AbstractKey... keys) {
        FieldCollector fieldCollector = new FieldCollector(keys);
        fieldCollector.addAll(list);
        return mapper.apply(fieldCollector.collectMax(corpusNames));
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
}
