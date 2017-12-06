package munch.data.place.matcher;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
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

    private final NameCleaner nameCleaner;

    @Inject
    public NameMatcher(NameCleaner nameCleaner) {
        this.nameCleaner = nameCleaner;
    }

    /**
     * @param insides data already inside
     * @param outside data outside coming in
     * @return true is outside data belongs with inside
     */
    public boolean match(List<CorpusData> insides, CorpusData outside) {
        List<String> outsideNames = collectNames(outside);

        for (CorpusData inside : insides) {
            List<String> insideNames = collectNames(inside);
            if (match(insideNames, outsideNames)) {
                return true;
            }
        }
        return false;
    }

    private List<String> collectNames(CorpusData data) {
        return PlaceKey.name.getAll(data).stream()
                .map(field -> nameCleaner.clean(field.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * @param lefts  multiple name
     * @param rights multiple name
     * @return true if matched
     */
    private boolean match(List<String> lefts, List<String> rights) {
        for (String left : lefts) {
            for (String right : rights) {
                if (match(left, right)) return true;
            }
        }
        return false;
    }

    /**
     * @param left  trimmed lower case
     * @param right lower case right
     * @return true = potentially equal place right
     */
    private boolean match(String left, String right) {
        return left.equalsIgnoreCase(right);
    }
}
