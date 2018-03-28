package munch.data.location;

import munch.data.utils.PatternSplit;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * Created by: Fuxing
 * Date: 27/3/2018
 * Time: 1:38 AM
 * Project: munch-data
 */
public abstract class CityParser {
    PatternSplit WHITESPACE_PATTERN = PatternSplit.compile(",? +");

    /**
     * @param text to parse
     * @return LocationData if found
     */
    @Nullable
    public LocationData parse(String text) {
        text = StringUtils.lowerCase(text);
        return parse(WHITESPACE_PATTERN.split(text));
    }

    /**
     * @param tokens in text to parse
     * @return LocationData if found
     */
    @Nullable
    abstract LocationData parse(List<String> tokens);

    /**
     * @param tokens        tokens to try parse
     * @param parseFunction parse function
     * @return Token parsed
     */
    protected static String parse(List<String> tokens, Function<String, String> parseFunction) {
        for (String token : tokens) {
            String apply = parseFunction.apply(token);
            if (apply != null) return apply;
        }

        return null;
    }

    protected static String has(List<String> tokens, String... compares) {
        for (String compare : compares) {
            if (tokens.contains(compare)) {
                return compare;
            }
        }

        return null;
    }
}
