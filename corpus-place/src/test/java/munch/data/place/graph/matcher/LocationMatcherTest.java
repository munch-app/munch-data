package munch.data.place.graph.matcher;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

/**
 * Created by: Fuxing
 * Date: 25/5/18
 * Time: 3:19 PM
 * Project: munch-data
 */
class LocationMatcherTest {

    private void test(String unit, String left, String right) {
        Optional<Pair<String, String>> match = LocationMatcher.match(unit);
        String actualLeft = match.map(Pair::getLeft).orElse(null);
        String actualRight = match.map(Pair::getRight).orElse(null);
        Assertions.assertEquals(left, actualLeft);
        Assertions.assertEquals(right, actualRight);
    }

    @Test
    void unit() {
        test("#05-497", "5", "497");
        test("#05-454545", "5", "454545");
        test("#05", "5", null);
        test("05-497", "5", "497");
        test("#05-", "5", null);
        test("#05-07", "5", "7");
        test("#111-07", "111", "7");
    }
}