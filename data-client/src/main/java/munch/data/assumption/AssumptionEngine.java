package munch.data.assumption;

import com.google.common.base.Joiner;
import munch.data.structure.SearchQuery;
import munch.data.utils.PatternSplit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

/**
 * Created by: Fuxing
 * Date: 23/2/18
 * Time: 8:50 PM
 * Project: munch-data
 */
@Singleton
public final class AssumptionEngine {
    public static final Set<String> STOP_WORDS = Set.of("around", "near", "in", "at", "food", "and", "or");
    public static final PatternSplit TOKENIZE_PATTERN = PatternSplit.compile(" {1,}|,|\\.");
    private final AssumptionDatabase database;

    @Inject
    public AssumptionEngine(CachedAssumptionDatabase database) {
        this.database = database;
    }

    AssumptionEngine(AssumptionDatabase database) {
        this.database = database;
    }

    public Optional<AssumedSearchQuery> assume(SearchQuery prevQuery, String text) {
        Map<String, Assumption> assumptionMap = database.get();
        text = text.trim();
        List<Object> tokenList = tokenize(assumptionMap, text);

        if (tokenList.isEmpty()) return Optional.empty();
        if (tokenList.size() == 1 && tokenList.get(0) instanceof String) return Optional.empty();

        List<AssumedSearchQuery.Token> assumedTokens = new ArrayList<>();
        for (Object token : tokenList) {
            if (token instanceof String) {
                // Stop-words checking
                String textToken = (String) token;
                if (!STOP_WORDS.contains(textToken)) return Optional.empty();
                assumedTokens.add(new AssumedSearchQuery.TextToken(textToken));
            } else {
                Assumption assumption = (Assumption) token;
                assumption.apply(prevQuery);
                assumedTokens.add(new AssumedSearchQuery.TagToken(assumption.getTag()));
            }
        }

        AssumedSearchQuery query = new AssumedSearchQuery();
        query.setText(text);
        query.setTokens(assumedTokens);
        query.setQuery(prevQuery);
        return Optional.of(query);
    }

    private List<Object> tokenize(Map<String, Assumption> assumptionMap, String text) {
        if (StringUtils.isBlank(text)) return List.of();
        Assumption assumption = assumptionMap.get(text.toLowerCase());
        if (assumption != null) return List.of(assumption);


        List<String> parts = TOKENIZE_PATTERN.splitRemoved(text);
        for (int i = 2; i < parts.size() + 1; i++) {
            for (Triple<String, String, String> combo : reverseSplitInto(parts, i)) {
                assumption = assumptionMap.get(combo.getMiddle().toLowerCase());
                if (assumption != null) {
                    // Found Assumption
                    List<Object> tokenList = new ArrayList<>();
                    tokenList.add(assumption);
                    if (combo.getLeft() != null) {
                        tokenList.addAll(0, tokenize(assumptionMap, combo.getLeft()));
                    }
                    if (combo.getRight() != null) {
                        tokenList.addAll(tokenize(assumptionMap, combo.getRight()));
                    }
                    return tokenList;
                }
            }
        }

        // Not Found
        return List.of(text);
    }

    private void joinStrings(List<Object> tokens) {
        ListIterator<Object> iterator = tokens.listIterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (next instanceof String && iterator.hasNext()) {
                Object nextNext = iterator.next();
                if (nextNext instanceof String) {
                    iterator.remove();
                    iterator.previous();
                    iterator.set(next + " " + nextNext);
                }
            }
        }
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
