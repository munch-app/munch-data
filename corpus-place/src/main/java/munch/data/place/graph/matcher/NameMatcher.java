package munch.data.place.graph.matcher;


import corpus.data.CorpusData;
import corpus.field.FieldUtils;
import corpus.field.PlaceKey;
import info.debatty.java.stringsimilarity.experimental.Sift4;
import info.debatty.java.stringsimilarity.interfaces.StringDistance;
import munch.data.place.matcher.NameCleaner;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 30/3/2018
 * Time: 9:42 PM
 * Project: munch-data
 */
@Singleton
public final class NameMatcher implements Matcher {
    private static final StringDistance distance = new Sift4();

    private final NameCleaner nameCleaner;
    private final StopWords stopWords;

    @Inject
    public NameMatcher(NameCleaner nameCleaner, StopWords stopWords) {
        this.nameCleaner = nameCleaner;
        this.stopWords = stopWords;
    }

    // FUTURE: LOOSE Name Matching?

    @Override
    public Map<String, Integer> match(String placeId, CorpusData left, CorpusData right) {
        List<String> leftNames = collectPlaceNames(left);

        // Might want to join both left & right name
        if (!leftNames.isEmpty()) {
            List<String> rightNames = collectPlaceNames(right);
            // Place.name to Place.name
            if (!rightNames.isEmpty()) {
                if (match(leftNames, rightNames)) {
                    return Map.of("Place.name", 1);
                }

                for (String rightName : rightNames) {
                    for (String leftName : leftNames) {
                        if (StringUtils.startsWithAny(rightName, leftName) && rightName.length() > 10) {
                            return Map.of("Place.name.contains",  1);
                        }
                    }
                }
                return Map.of("Place.name",  -1);
            } else {
                // Place.name to Article.Place.names
                List<String> articleNames = collectArticleNames(right);
                return Map.of("Place.name", articleMatch(leftNames, articleNames) ? 1 : -1);
            }
        }

        return matchArticleNames(left, right);
    }

    private Map<String, Integer> matchArticleNames(CorpusData left, CorpusData right) {
        // Article.Place.names to Article.Place.names
        List<String> leftArticleNames = collectArticleNames(left);
        List<String> rightArticleNames = collectArticleNames(right);
        if (leftArticleNames.size() != 1 || rightArticleNames.size() != 1) return Map.of();

        // Articles Names only matcher is very strict
        if (leftArticleNames.get(0).equalsIgnoreCase(rightArticleNames.get(0))) {
            return Map.of("Article.Place.names", 1);
        }
        return Map.of();
    }

    @Override
    public Set<String> requiredFields() {
        return Set.of("Place.name", "Article.Place.names");
    }

    private List<String> collectPlaceNames(CorpusData data) {
        return PlaceKey.name.getAll(data).stream()
                .map(field -> nameCleaner.clean(field.getValue()))
                .map(String::toLowerCase)
                // Temporary And Solution
                .map(s -> s.replace(" & ", "and"))
                .collect(Collectors.toList());
    }

    private List<String> collectArticleNames(CorpusData data) {
        return FieldUtils.getAll(data, "Article.Place.names").stream()
                .map(field -> nameCleaner.simpleClean(field.getValue()))
                .map(String::toLowerCase)
                // Temporary And Solution
                .map(s -> s.replace(" & ", "and"))
                .collect(Collectors.toList());
    }

    private boolean articleMatch(List<String> placeNames, List<String> articleNames) {
        for (String left : placeNames) {
            for (String right : articleNames) {
                if (match(left, right)) return true;
                if (containsMatch(left, right)) return true;
            }
        }
        return false;
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
        if (left.length() > 12) {
            return distance.distance(left, right) <= 1.0;
        }
        return left.equalsIgnoreCase(right);
    }

    /**
     * @param placeName        actual place name
     * @param articlePlaceName article place name
     * @return true if contains match
     */
    private boolean containsMatch(String placeName, String articlePlaceName) {
        if (articlePlaceName.contains(placeName)) {
            int startIndex = articlePlaceName.indexOf(placeName);
            int endIndex = startIndex + placeName.length();
            String left = articlePlaceName.substring(0, startIndex);
            String right = articlePlaceName.substring(endIndex, articlePlaceName.length());

            // if both left & right is validated
            return validateLeft(left) && validateRight(right);
        }
        return false;
    }

    /**
     * @param text left text to validate
     * @return true if text on the left is allowed
     */
    private boolean validateLeft(String text) {
        if (StringUtils.isBlank(text)) return true;

        // Check that text is a word, hence have spacing on last index
        if (text.substring(text.length() - 1).equals(" ")) {
            text = text.trim();
            // Only allowed if is shorter or equal to 5 characters
            if (text.length() <= 5) return true;
        }

        return false;
    }

    /**
     * @param text right text to validate
     * @return true if text on the right is allowed
     */
    private boolean validateRight(String text) {
        if (StringUtils.isBlank(text)) return true;
        // Check that text is a word, hence have spacing on index 0
        if (!text.substring(0, 1).equals(" ")) return false;
        text = text.trim();

        // Allowed if is shorter or equal to 5 characters
        if (text.length() <= 5) return true;

        // Else run through stop word list
        String cleaned = stopWords.clean(text);
        // Allowed only if text is shorter then length 2
        return StringUtils.length(cleaned) <= 2;
    }
}
