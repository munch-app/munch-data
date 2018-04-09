package munch.data.location;

import munch.data.utils.PatternSplit;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 27/3/2018
 * Time: 1:38 AM
 * Project: munch-data
 */
public abstract class CityParser {
    // TODO []():;'"@\\n\\r-={}|\/<>
    // |\\n\\r|\\r\\n|\\n|\\r|-|–|—|:|@|\||\\
    public static Pattern REPLACE_SPACE_PATTERN = Pattern.compile("\\\\n|\\\\r|\\\\t");
    public static PatternSplit WHITESPACE_PATTERN = PatternSplit.compile("[,.()'\"]?( +|$)|( +|^)[,.()'\"]");

    /**
     * @param text to parse
     * @return LocationData if found
     */
    @Nullable
    public LocationData parse(String text) {
        return parse(WHITESPACE_PATTERN.split(text));
    }

    /**
     * @param tokens in text to parse
     * @return LocationData if found
     */
    @Nullable
    abstract LocationData parse(List<String> tokens);

    public static List<String> parseTokens(String text) {
        text = StringUtils.lowerCase(text);
        text = REPLACE_SPACE_PATTERN.matcher(text).replaceAll(" ");

        return WHITESPACE_PATTERN.split(text);
    }

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

    protected static boolean has(List<String> tokens, String... compares) {
        for (String compare : compares) {
            if (tokens.contains(compare)) {
                return true;
            }
        }

        return false;
    }
}
