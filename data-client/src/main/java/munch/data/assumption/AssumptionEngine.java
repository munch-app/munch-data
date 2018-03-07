package munch.data.assumption;

import com.google.common.base.Joiner;
import munch.data.clients.LocationUtils;
import munch.data.clients.PlaceClient;
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
public class AssumptionEngine {
    public static final Set<String> STOP_WORDS = Set.of("around", "near", "in", "at", "food", "and", "or", "cuisine");
    public static final PatternSplit TOKENIZE_PATTERN = PatternSplit.compile(" {1,}|,|\\.");

    private final AssumptionDatabase database;
    private final PlaceClient.SearchClient searchClient;

    @Inject
    public AssumptionEngine(CachedAssumptionDatabase database, PlaceClient.SearchClient searchClient) {
        this.database = database;
        this.searchClient = searchClient;
    }

    AssumptionEngine(AssumptionDatabase database, PlaceClient.SearchClient searchClient) {
        this.database = database;
        this.searchClient = searchClient;
    }

    public List<AssumedSearchQuery> assume(SearchQuery query, String text) {
        Map<String, Assumption> assumptionMap = database.get();
        text = text.trim();
        List<Object> tokenList = tokenize(assumptionMap, text);

        // Validate
        if (tokenList.isEmpty()) return List.of();
        if (tokenList.size() == 1) {
            // Only one token, check if token is explicit
            Object token = tokenList.get(0);
            if (token instanceof String) return List.of();
        }
        for (Object token : tokenList) {
            if (token instanceof String) {
                // Stop-words checking
                String textToken = (String) token;
                String[] parts = textToken.split(" +");
                for (String part : parts) {
                    // If any part is not stop word, return empty
                    if (!STOP_WORDS.contains(part)) return List.of();
                }
            }
        }

        return createList(query, text, tokenList);
    }

    private List<AssumedSearchQuery> createList(SearchQuery query, String text, List<Object> tokenList) {
        List<AssumedSearchQuery.Token> assumedTokens = asToken(tokenList);
        List<Assumption> assumptions = asAssumptions(tokenList);
        query = createSearchQuery(query, assumptions);

        String location = getLocation(tokenList);
        if (location != null) return List.of(createAssumedQuery(location, text, assumedTokens, query));

        List<AssumedSearchQuery> list = new ArrayList<>();
        createCurrent(text, assumedTokens, query).ifPresent(list::add);
        createNearby(text, assumedTokens, query).ifPresent(list::add);
        createAnywhere(text, assumedTokens, query).ifPresent(list::add);
        return list;
    }

    private Optional<AssumedSearchQuery> createAnywhere(String text, List<AssumedSearchQuery.Token> assumedTokens, SearchQuery query) {
        query = query.deepCopy();
        query.getFilter().setLocation(LocationUtils.SINGAPORE);
        query.getFilter().setContainers(List.of());

        return Optional.of(createAssumedQuery("Anywhere", text, assumedTokens, query));
    }

    private Optional<AssumedSearchQuery> createNearby(String text, List<AssumedSearchQuery.Token> assumedTokens, SearchQuery query) {
        if (query.getLatLng() == null) return Optional.empty();

        query = query.deepCopy();
        query.setRadius(LocationUtils.DEFAULT_RADIUS);
        query.getFilter().setLocation(null);
        query.getFilter().setContainers(List.of());

        AssumedSearchQuery nearby = createAssumedQuery("Nearby", text, assumedTokens, query);
        if (nearby.getResultCount() == 0) return Optional.empty();
        // Check results count is 0
        return Optional.of(nearby);
    }

    private Optional<AssumedSearchQuery> createCurrent(String text, List<AssumedSearchQuery.Token> assumedTokens, SearchQuery query) {
        query = query.deepCopy();

        if (LocationUtils.isAnywhere(query)) return Optional.empty();
        String locationName = LocationUtils.getName(query, null);
        if (locationName == null) return Optional.empty();

        if (locationName.equalsIgnoreCase("singapore")) return Optional.empty();
        if (locationName.equalsIgnoreCase("anywhere")) return Optional.empty();

        AssumedSearchQuery current = createAssumedQuery(locationName, text, assumedTokens, query);
        if (current.getResultCount() == 0) return Optional.empty();
        // Check results count is 0
        return Optional.of(current);
    }

    protected AssumedSearchQuery createAssumedQuery(String location, String text,
                                                    List<AssumedSearchQuery.Token> assumedTokens, SearchQuery query) {
        AssumedSearchQuery assumedSearchQuery = new AssumedSearchQuery();
        assumedSearchQuery.setText(text);
        assumedSearchQuery.setLocation(location);
        assumedSearchQuery.setResultCount(searchClient.count(query));

        assumedSearchQuery.setTokens(assumedTokens);
        assumedSearchQuery.setSearchQuery(query);
        return assumedSearchQuery;
    }

    private static SearchQuery createSearchQuery(SearchQuery query, List<Assumption> assumptions) {
        query = query.deepCopy();
        for (Assumption assumption : assumptions) {
            assumption.apply(query);
        }
        return query;
    }

    private static String getLocation(List<Object> tokenList) {
        for (Object o : tokenList) {
            if (o instanceof Assumption) {
                Assumption assumption = (Assumption) o;
                if (Assumption.Type.Location.equals(assumption.getType())) {
                    return assumption.getTag();
                }
            }
        }
        return null;
    }

    private static List<AssumedSearchQuery.Token> asToken(List<Object> tokenList) {
        List<AssumedSearchQuery.Token> assumedTokens = new ArrayList<>();
        for (Object o : tokenList) {
            if (o instanceof String) {
                assumedTokens.add(new AssumedSearchQuery.TextToken((String) o));
            } else {
                assumedTokens.add(new AssumedSearchQuery.TagToken(((Assumption) o).getTag()));
            }
        }
        return assumedTokens;
    }

    private static List<Assumption> asAssumptions(List<Object> tokenList) {
        List<Assumption> tokens = new ArrayList<>();
        for (Object o : tokenList) {
            if (o instanceof Assumption) {
                tokens.add((Assumption) o);
            }
        }
        return tokens;
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
