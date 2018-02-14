package munch.data.hour.tokens;

import munch.data.utils.PatternSplit;
import munch.data.utils.PatternTexts;

import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 14/2/18
 * Time: 11:48 AM
 * Project: munch-data
 */
public class RemoveToken {
    static final PatternSplit PatternMeal = PatternSplit.compile("\\b(breakfast|lunch|dinner|supper|brunch)s?\\b", Pattern.CASE_INSENSITIVE);
    static final PatternSplit PatternSymbol = PatternSplit.compile("[:()]", Pattern.CASE_INSENSITIVE);

    public static void parse(PatternTexts texts) {
        texts.replace(PatternMeal, m -> null);
        texts.replace(PatternSymbol, m -> null);
    }
}
