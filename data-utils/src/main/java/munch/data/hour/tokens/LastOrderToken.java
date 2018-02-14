package munch.data.hour.tokens;

import munch.data.utils.PatternSplit;
import munch.data.utils.PatternTexts;

import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 14/2/18
 * Time: 12:01 PM
 * Project: munch-data
 */
public class LastOrderToken {
    static final PatternSplit PatternLastOrder = PatternSplit.compile("\\blast order at|last order\\b", Pattern.CASE_INSENSITIVE);

    public static void parse(PatternTexts texts) {
        texts.replace(PatternLastOrder, m -> new LastOrderToken());
        texts.remove(LastOrderToken.class, TimeToken.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 1233154901;
    }

    @Override
    public String toString() {
        return "(Last Order)";
    }
}
