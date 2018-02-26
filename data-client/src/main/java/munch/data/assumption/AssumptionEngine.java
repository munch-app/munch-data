package munch.data.assumption;

import com.google.common.base.Joiner;
import munch.data.structure.SearchQuery;
import munch.data.utils.PatternSplit;
import org.apache.commons.lang3.tuple.Triple;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 23/2/18
 * Time: 8:50 PM
 * Project: munch-data
 */
@Singleton
public final class AssumptionEngine {
    public static final Set<String> STOP_WORD = Set.of("around", "near", "in", "at", ",", ".");
    public static final PatternSplit TOKENIZE_PATTERN = PatternSplit.compile(" {1,}|,");
    private final AssumptionDatabase database;

    @Inject
    public AssumptionEngine(CachedAssumptionDatabase database) {
        this.database = database;
    }

    public AssumedSearchQuery assume(SearchQuery prevQuery, String text) {
        Map<String, Assumption> assumptionMap = database.get();
        text = text.trim();
        return null;

    }

    private List<Object> tokenize(Map<String, Assumption> assumptionMap, String text) {
        Assumption assumption = assumptionMap.get(text.toLowerCase());
        if (assumption != null) return List.of(assumption);

        List<String> parts = TOKENIZE_PATTERN.splitRemoved(text);
        for (int i = 2; i < parts.size() - 1; i++) {
            for (Triple<String, String, String> combo : reverseSplitInto(parts, i)) {
                List<Object> tokenized = tokenize(assumptionMap, combo.getMiddle());
                if (tokenized.isEmpty()) continue;

                if (combo.getLeft() != null) {
                    tokenized.addAll(0, tokenize(assumptionMap, combo.getLeft()));
                }
                if (combo.getRight() != null) {
                    tokenized.addAll(tokenize(assumptionMap, combo.getRight()));
                }
                return tokenized;
            }
        }

        // Non Found
        return List.of(text);
    }


    public static List<Triple<String, String, String>> splitInto(List<String> parts, int join) {
        List<Triple<String, String, String>> joined = new ArrayList<>();
        for (int i = 0; i < parts.size() - join; i++) {
            String left = Joiner.on(" ").join(parts.subList(0, i));
            String middle = Joiner.on(" ").join(parts.subList(i, i + join + 1));
            String right = Joiner.on(" ").join(parts.subList(i + join + 1, parts.size()));
            joined.add(Triple.of(left, middle, right));
        }
        return joined;
    }

    public static List<Triple<String, String, String>> reverseSplitInto(List<String> parts, int combination) {
        return splitInto(parts, combination > parts.size() ? 0 : parts.size() - combination);
    }
}
