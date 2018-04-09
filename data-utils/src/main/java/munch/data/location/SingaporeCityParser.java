package munch.data.location;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 27/3/2018
 * Time: 1:38 AM
 * Project: munch-data
 */
@Singleton
public final class SingaporeCityParser extends CityParser {

    private static final Pattern PostalPattern = Pattern.compile("(singapore|sg|s|pore)([ \n(])*(?<postal>[0-9]{5,6})", Pattern.CASE_INSENSITIVE);
    private static final Pattern Trailing = Pattern.compile("(?<postal>\\b[0-9]{6}\\b|\\b[0-9]{5}$)");

    private static final Pattern StreetNumberPattern = Pattern.compile("[0-9]{1,4}[a-z]?");
    private static final Pattern UnitNumberPattern = Pattern.compile("[0-9]{1,2}-[0-9]{1,5}");

    private final StreetSuffixDatabase suffixDatabase;

    @Inject
    public SingaporeCityParser(StreetSuffixDatabase suffixDatabase) {
        this.suffixDatabase = suffixDatabase;
    }

    @Nullable
    @Override
    public LocationData parse(List<String> tokens) {
        LocationData data = new LocationData(tokens);
        data.setPostal(parsePostalToken(tokens));
        data.setStreet(parseStreetToken(tokens));
        data.setUnitNumber(parseUnitNumberToken(tokens));
        data.setCity(has(tokens, "singapore", "sg") ? "singapore" : null);
        data.setCountry(data.getCity());

        if (data.getPostal() != null) {
            if (data.getCity() != null) return data;
            if (data.getStreet() != null) return data;
            if (data.getUnitNumber() != null) return data;
        }

        if (data.getUnitNumber() != null && data.getStreet() != null) return data;
        if (tokens.contains("singapore") && data.getStreet() != null) return data;
        return null;
    }

    private static String parsePostalToken(List<String> tokens) {
        for (String token : tokens) {
            if (StringUtils.isBlank(token)) continue;
            if (NumberUtils.isDigits(token) && token.length() >= 5 && token.length() <= 6) return token;

            String postal = findGroup(PostalPattern, token, "postal");
            if (postal != null) return postal;
        }
        return null;
    }

    private static String findGroup(Pattern pattern, String text, String name) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String group = matcher.group(name);
            if (group != null) return group;
        }
        return null;
    }

    private static String parseUnitNumberToken(List<String> tokens) {
        int stallStage = 0;

        for (String token : tokens) {
            if (StringUtils.isBlank(token)) continue;
            if (token.contains("#") && token.contains("-")) return token;
            if (UnitNumberPattern.matcher(token).matches()) return token;

            if (token.equalsIgnoreCase("stall")) {
                stallStage = 1;
                continue;
            }

            if (stallStage == 1) {
                if (token.equalsIgnoreCase("no")) {
                    stallStage = 2;
                    continue;
                }
                if (NumberUtils.isDigits(token)) return token;
            } else if (stallStage == 2) {
                if (NumberUtils.isDigits(token)) return token;
            }

            stallStage = 0;
        }
        return null;
    }

    private String parseStreetToken(List<String> tokens) {
        List<String> streetTokens = new ArrayList<>();
        for (String token : tokens) {
            if (StreetNumberPattern.matcher(token).matches()) {
                streetTokens.clear();
                streetTokens.add(token);
                continue;
            }

            if (!streetTokens.isEmpty()) {
                streetTokens.add(token);

                if (streetTokens.size() < 8 && suffixDatabase.is(token)) {
                    return Joiner.on("").join(streetTokens);
                }
            }
        }

        return null;
    }

    /**
     * @param text address with postal
     * @return postal if found else null
     */
    public static String parsePostal(String text) {
        if (StringUtils.isBlank(text)) return null;

        String postal = findGroup(PostalPattern, text, "postal");
        if (postal != null) return postal;

        postal = findGroup(Trailing, text, "postal");
        if (postal != null) return postal;

        return postal;
    }
}
