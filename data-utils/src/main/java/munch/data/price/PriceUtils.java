package munch.data.price;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 17/12/2017
 * Time: 6:29 PM
 * Project: munch-data
 */
public final class PriceUtils {
    private static final Pattern PricePattern = Pattern.compile("\\$(?<price>[0-9]{1,3}(\\.[0-9]{1,2})?)");

    public static List<Double> extract(String text) {
        Matcher matcher = PricePattern.matcher(text);
        List<Double> prices = new ArrayList<>();
        while (matcher.find()) {
            String price = matcher.group("price");
            prices.add(Double.parseDouble(price));
        }
        return prices;
    }

    public static List<String> extractString(String text) {
        Matcher matcher = PricePattern.matcher(text);
        List<String> prices = new ArrayList<>();
        while (matcher.find()) {
            prices.add(matcher.group("price"));
        }
        return prices;
    }

    public static boolean isPrice(String text) {
        if (StringUtils.isBlank(text)) return false;
        return PricePattern.matcher(text.trim()).matches();
    }
}
