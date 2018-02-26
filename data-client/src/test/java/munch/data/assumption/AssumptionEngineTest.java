package munch.data.assumption;

import munch.data.utils.PatternSplit;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Created by: Fuxing
 * Date: 26/2/18
 * Time: 7:27 PM
 * Project: munch-data
 */
class AssumptionEngineTest {

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
    void breakInto() {

    }
}