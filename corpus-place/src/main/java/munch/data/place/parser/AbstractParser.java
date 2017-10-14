package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.AbstractKey;
import corpus.utils.FieldCollector;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 11:56 PM
 * Project: munch-data
 */
public abstract class AbstractParser {

    @Nullable
    protected String collectMax(List<CorpusData> list, AbstractKey... keys) {
        FieldCollector fieldCollector = new FieldCollector(keys);
        fieldCollector.addAll(list);
        return fieldCollector.collectMax();
    }

    protected List<CorpusData.Field> filter(List<CorpusData> list, AbstractKey... keys) {
        FieldCollector fieldCollector = new FieldCollector(keys);
        fieldCollector.addAll(list);
        return fieldCollector.collectField();
    }

    protected boolean hasAny(AbstractKey key, List<CorpusData> list) {
        for (CorpusData data : list) {
            if (key.has(data)) return true;
        }
        return false;
    }
}
