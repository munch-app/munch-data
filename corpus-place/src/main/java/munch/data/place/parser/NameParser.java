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
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 18/12/2017
 * Time: 10:58 AM
 * Project: munch-data
 */
@Singleton
public final class NameParser extends AbstractParser<String> {
    private static final Set<String> BLOCKED_NAMES = Set.of("chinese characters", "chinese character", "chinese letter", "chinese letters");
    private static final Pattern BLOCKED_PATTERN = Pattern.compile("stalls? [0-9]+", Pattern.CASE_INSENSITIVE);

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

        // Validate name
        if (!validateName(name)) return null;

        // Then capitalize fully name
        return WordUtils.capitalizeFully(name);
    }

    /**
     * @param name name to validate
     * @return true = allowed
     */
    private boolean validateName(String name) {
        name = name.toLowerCase();
        if (BLOCKED_NAMES.contains(name)) return false;
        if (BLOCKED_PATTERN.matcher(name).matches()) return false;
        return true;
    }
}
