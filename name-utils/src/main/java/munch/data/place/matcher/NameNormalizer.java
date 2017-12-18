package munch.data.place.matcher;

import javax.inject.Singleton;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 6/12/2017
 * Time: 10:40 AM
 * Project: munch-data
 */
@Singleton
public final class NameNormalizer {
    private static final Pattern PATTERN_MULTI_SPACE = Pattern.compile(" {2,}");

    private static final List<ReplacementGroup> GROUPS = List.of(
            new ReplacementGroup(Pattern.compile("[`\"’]"), "'"),
            new ReplacementGroup(Pattern.compile("-|–|—"), "-"),
            new ReplacementGroup(Pattern.compile("á|â|à", Pattern.CASE_INSENSITIVE), "a"),
            new ReplacementGroup(Pattern.compile("é|ê|è|ë|ē", Pattern.CASE_INSENSITIVE), "e"),
            new ReplacementGroup(Pattern.compile("î|ï", Pattern.CASE_INSENSITIVE), "i"),
            new ReplacementGroup(Pattern.compile("ó|ô|ò", Pattern.CASE_INSENSITIVE), "o"),
            new ReplacementGroup(Pattern.compile("û|ù|ü", Pattern.CASE_INSENSITIVE), "u"),
            new ReplacementGroup(Pattern.compile("ÿ", Pattern.CASE_INSENSITIVE), "y"),
            new ReplacementGroup(Pattern.compile("ç", Pattern.CASE_INSENSITIVE), "c")
    );

    public String normalize(String name) {
        // Iterate through all of replacement group
        for (ReplacementGroup group : GROUPS) {
            name = group.replace(name);
        }
        // Remove and trim multiple spaces "  " -> " "
        return trim(name);
    }

    /**
     * Remove whitespace by trimming from 2+ to 1
     * Remove trailing and leading whitespace
     *
     * @param text text to trim
     * @return trimmed
     */
    public static String trim(String text) {
        return PATTERN_MULTI_SPACE
                .matcher(text)
                .replaceAll(" ")
                .trim();
    }

    static class ReplacementGroup {
        private final Pattern pattern;
        private final String replacement;

        ReplacementGroup(Pattern pattern, String replacement) {
            this.pattern = pattern;
            this.replacement = replacement;
        }

        public String replace(String text) {
            return pattern.matcher(text).replaceAll(replacement);
        }
    }
}
