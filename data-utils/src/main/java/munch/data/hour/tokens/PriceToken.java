package munch.data.hour.tokens;

import munch.data.utils.PatternSplit;
import munch.data.utils.PatternTexts;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Created to remove false positive from hour tokens
 * <p>
 * Created by: Fuxing
 * Date: 14/2/18
 * Time: 3:17 PM
 * Project: munch-data
 */
public class PriceToken {
    static final PatternSplit PatternPrice = PatternSplit.compile("\\$(?<price>[0-9]{1,3}(\\.[0-9]{1,2})?)", Pattern.CASE_INSENSITIVE);

    public final String text;

    private PriceToken(String text) {
        this.text = text;
    }

    public static void parse(PatternTexts texts) {
        texts.replace(PatternPrice, matcher -> new PriceToken(matcher.group("price")));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PriceToken that = (PriceToken) o;
        return Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    @Override
    public String toString() {
        return "(Price: " + text + ")";
    }
}
