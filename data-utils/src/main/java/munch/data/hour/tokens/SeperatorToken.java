package munch.data.hour.tokens;

import munch.data.utils.PatternSplit;
import munch.data.utils.PatternTexts;

import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 14/2/18
 * Time: 11:55 AM
 * Project: munch-data
 */
public class SeperatorToken {
    static final PatternSplit PatternLineBreak = PatternSplit.compile("\\\\n\\\\r|\\\\r\\\\n|\\\\n|\\\\r", Pattern.CASE_INSENSITIVE);

    public static void parse(PatternTexts texts) {
        texts.replace(PatternLineBreak, m -> ",");
    }
}
