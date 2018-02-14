package munch.data.hour.tokens;

import munch.data.utils.PatternSplit;
import munch.data.utils.PatternTexts;

import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 14/2/18
 * Time: 11:51 AM
 * Project: munch-data
 */
public class RangeToken {
    static final PatternSplit PatternRange = PatternSplit.compile("(\\bto\\b|-|–|—)", Pattern.CASE_INSENSITIVE);

    public static void parse(PatternTexts texts) {
        texts.replace(PatternRange, new RangeToken());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 2082814191;
    }

    @Override
    public String toString() {
        return "(Range)";
    }
}
