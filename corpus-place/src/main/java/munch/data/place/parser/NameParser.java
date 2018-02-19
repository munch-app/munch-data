package munch.data.place.parser;

import com.google.common.base.Joiner;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import corpus.utils.FieldCollector;
import munch.data.place.matcher.NameNormalizer;
import munch.data.structure.Place;
import munch.data.utils.PatternSplit;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 18/12/2017
 * Time: 10:58 AM
 * Project: munch-data
 */
@Singleton
public final class NameParser extends AbstractParser<String> {
    private static final PatternSplit NAME_DIVIDER_PATTERN = PatternSplit.compile("([^a-z']|^|[^a-z]')[a-z]");
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

        // Then format name properly
        return format(name);
    }

    public static String format(String name) {
        List<Object> split = NAME_DIVIDER_PATTERN.split(name.toLowerCase(), 0, String::toUpperCase);
        return Joiner.on("").join(split);
    }

    /**
     * @param place place
     * @param list  list of CorpusData
     * @return set of all names
     */
    public Set<String> parseAllNames(Place place, List<CorpusData> list) {
        FieldCollector fieldCollector = new FieldCollector(PlaceKey.name);
        fieldCollector.addAll(list);

        return fieldCollector.collect()
                .stream()
                .map(StringUtils::lowerCase)
                .map(nameNormalizer::normalize)
                .filter(this::validateName)
                .collect(Collectors.toSet());
    }

    /**
     * @param name name to validate
     * @return true = allowed
     */
    private boolean validateName(String name) {
        name = name.toLowerCase();
        if (BLOCKED_PATTERN.matcher(name).matches()) return false;
        return true;
    }
}
