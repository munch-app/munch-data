package munch.data.assumption;

import munch.data.structure.SearchQuery;
import munch.data.utils.PatternSplit;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 26/2/18
 * Time: 7:27 PM
 * Project: munch-data
 */
class AssumptionEngineTest {

    private AssumptionEngine assumptionEngine;

    @BeforeEach
    void setUp() {
        assumptionEngine = new AssumptionEngine(new AssumptionDatabase(null) {
            @Override
            public Map<String, Assumption> get() {
                Map<String, Assumption> assumptionMap = new HashMap<>();
                EXPLICIT_ASSUMPTION.forEach(assumption -> assumptionMap.put(assumption.getToken(), assumption));
                return assumptionMap;
            }
        }, null) {
            @Override
            protected AssumedSearchQuery createAssumedQuery(String location, String text, List<AssumedSearchQuery.Token> assumedTokens, SearchQuery query) {
                AssumedSearchQuery assumedSearchQuery = new AssumedSearchQuery();
                assumedSearchQuery.setText(text);
                assumedSearchQuery.setLocation(location);
                assumedSearchQuery.setTokens(assumedTokens);
                assumedSearchQuery.setSearchQuery(query);
                return assumedSearchQuery;
            }
        };
    }

    @Test
    void engine() {
        List<String> split = PatternSplit.compile(" ")
                .splitRemoved("Brown Cow");
        System.out.println(split);
    }

    @Test
    void reverse() {
        List<String> parts = AssumptionEngine.TOKENIZE_PATTERN.splitRemoved("Brown Cow Up Cash Cash");
        for (Triple<String, String, String> triple : AssumptionEngine.splitInto(parts, 1)) {
            System.out.println(triple);
        }
        System.out.println();

        for (Triple<String, String, String> triple : AssumptionEngine.reverseSplitInto(parts, 6)) {
            System.out.println(triple);
        }
    }

    @Test
    void assume() {
        System.out.println(assumptionEngine.assume(newSearchQuery(), "open now near me"));
        System.out.println(assumptionEngine.assume(newSearchQuery(), "open now in Singapore"));
        System.out.println(assumptionEngine.assume(newSearchQuery(), "open now in nearby me"));
        System.out.println(assumptionEngine.assume(newSearchQuery(), "bars open now nearby"));
        System.out.println(assumptionEngine.assume(newSearchQuery(), "drugs bars open now nearby"));
        System.out.println(assumptionEngine.assume(newSearchQuery(), "chinese food in nearby"));
    }

    @Test
    void single() throws Exception {
        System.out.println(assumptionEngine.assume(newSearchQuery(), "bars food in nearby"));
    }

    public SearchQuery newSearchQuery() {
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setUserInfo(new SearchQuery.UserInfo());
        searchQuery.getUserInfo().setDay("mon");
        searchQuery.getUserInfo().setTime("08:30");
        return searchQuery;
    }
}