package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import corpus.utils.FieldCollector;
import munch.data.place.matcher.NameNormalizer;
import munch.data.structure.Place;
import org.apache.commons.lang3.text.WordUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 18/12/2017
 * Time: 10:58 AM
 * Project: munch-data
 */
@Singleton
public final class NameParser extends AbstractParser<String> {

    private final NameNormalizer nameNormalizer;

    @Inject
    public NameParser(NameNormalizer nameNormalizer) {
        this.nameNormalizer = nameNormalizer;
    }

    @Override
    public String parse(Place place, List<CorpusData> list) {
        FieldCollector fieldCollector = new FieldCollector(PlaceKey.name);
        fieldCollector.addAll(list);

        String priorityName = fieldCollector.collectMax(priorityCorpus);
        if (priorityName != null) {
            // If priority name is found, it will be used without capitalize Fully
            return nameNormalizer.normalize(priorityName);
        }

        String name = fieldCollector.collectMax();
        // Normalize name first
        name = nameNormalizer.normalize(name);
        // Then capitalize fully name
        return WordUtils.capitalizeFully(name);
    }
}
