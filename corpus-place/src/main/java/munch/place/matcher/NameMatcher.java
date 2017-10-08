package munch.place.matcher;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import info.debatty.java.stringsimilarity.interfaces.StringSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 4/5/2017
 * Time: 10:25 PM
 * Project: munch-corpus
 */
@Singleton
public final class NameMatcher {
    private static final Logger logger = LoggerFactory.getLogger(NameMatcher.class);
    private static final StringSimilarity similarity = new NormalizedLevenshtein();
    private static final double threshold = 0.939;

    /**
     * @param insides data already inside
     * @param outside data outside coming in
     * @return true is outside data belongs with inside
     */
    public boolean match(List<CorpusData> insides, CorpusData outside) {
        // TODO Clean Name
        // TODO Location in Name
        // TODO Actual Matching
        return true;
    }

    /**
     * Match both postal and name
     *
     * @param data catalyst link with corpus data
     * @return true = same name
     */
    public boolean match(CorpusData data) {
        Optional<CorpusData.Field> field = PlaceKey.name.get(data);
        return field.filter(f -> match(f.getValue())).isPresent();
    }

    /**
     * @param name name of place to match
     * @return true if matched
     */
    protected boolean match(String name) {
        String cleaned = clean(name);
        return names.stream().anyMatch(saved -> match(saved, cleaned));
    }

    /**
     * @param saved trimmed lower case
     * @param name  lower case name
     * @return true = potentially equal place name
     */
    protected boolean match(String saved, String name) {
        double score = similarity.similarity(name, saved);
        logger.trace("{} and {} scored: {}", name, saved, score);
        return score > threshold;
    }

    /**
     * 1. lowercase it
     * 2. remove .,'"`
     * 3. replace - to space
     * 4. remove pte.? ltd.?
     *
     * @param text string to clean
     * @return cleaned string
     */
    protected String clean(String text) {

        return text.toLowerCase()
                // Divider Words
                .replaceAll("-|–|—|[_:;@|/\\\\~]", " ")
                // Only Compare AlphaNumeric
                .replaceAll("[`!#$%^&*()+=\\[\\]{}\">.<,?]", "")
                // Remove PTE LTD
                .replaceAll("pte ?ltd", "")
                // Trim all the 1 spacing
                .replaceAll(" {2,}", " ")
                // Trim starts and end
                .trim();
    }
}
