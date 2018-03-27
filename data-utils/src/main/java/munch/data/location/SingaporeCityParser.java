package munch.data.location;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 27/3/2018
 * Time: 1:38 AM
 * Project: munch-data
 */
public final class SingaporeCityParser implements CityParser {

    private static final Pattern PostalPattern = Pattern.compile("(singapore|sg|s|pore)([ \n(])*(?<postal>[0-9]{5,6})", Pattern.CASE_INSENSITIVE);
    private static final Pattern Trailing = Pattern.compile("(?<postal>\\b[0-9]{6}\\b|\\b[0-9]{5}$)");

    @Nullable
    @Override
    public LocationData parse(String text) {
        /*
        Unit|Vocab + Ending Postal
        Full Postal
         */

        return null;
    }

    /**
     * @param text address with postal
     * @return postal if found else null
     */
    public static String parsePostal(String text) {
        if (StringUtils.isBlank(text)) return null;

        String postal = null;
        // Full postal match
        Matcher matcher = PostalPattern.matcher(text);
        if (matcher.find()) postal = matcher.group("postal");
        if (postal != null) return postal;

        // Else find only number match or trailing match
        matcher = Trailing.matcher(text);
        if (matcher.find()) postal = matcher.group("postal");

        return postal;
    }
    // TODO implement
}
