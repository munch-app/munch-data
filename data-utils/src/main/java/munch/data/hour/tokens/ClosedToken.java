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
public class ClosedToken {
    static final PatternSplit PatternClosed = PatternSplit.compile("\\b(closed? on alternat(e|ive)|closed? on|closed? every|closed?)\\b", Pattern.CASE_INSENSITIVE);

    public static void parse(PatternTexts texts) {
        texts.replace(PatternClosed, new ClosedToken());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 1155194720;
    }

    @Override
    public String toString() {
        return "(Closed)";
    }
}
