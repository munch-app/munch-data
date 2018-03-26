package munch.data.location;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 21/5/2017
 * Time: 12:22 AM
 * Project: article-corpus
 */
public final class PostalParser {

    private static final Pattern PostalPattern = Pattern.compile("(singapore|sg|s|pore)([ \n(])*(?<postal>[0-9]{5,6})", Pattern.CASE_INSENSITIVE);
    private static final Pattern Trailing = Pattern.compile("(?<postal>\\b[0-9]{6}\\b|\\b[0-9]{5}$)");

    /**
     * @param text address with postal
     * @return postal if found else null
     */
    public static String parse(String text) {
        if (StringUtils.isBlank(text)) return null;

        String postal = null;
        // Full postal match
        Matcher matcher = PostalPattern.matcher(text);
        if (matcher.find()) postal = matcher.group("postal");
        if (postal != null) return postal;

        // Else find only number match or trailing match
        matcher = Trailing.matcher(text);
        if (matcher.find()) postal = matcher.group("postal");

        // Log down why cant be found
        // if (postal == null) {
        //    logger.warn("Postal not found for address: {}", address);
        // }
        return postal;
}
}
