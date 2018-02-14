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
public class AndToken {
    static final PatternSplit PatternAnd = PatternSplit.compile("\\band\\b|&|,|;", Pattern.CASE_INSENSITIVE);

    public static void parse(PatternTexts texts) {
        texts.replace(PatternAnd, new AndToken());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 378517922;
    }

    @Override
    public String toString() {
        return "(And)";
    }
}
