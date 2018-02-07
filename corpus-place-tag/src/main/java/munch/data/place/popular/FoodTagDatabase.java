package munch.data.place.popular;

import com.google.common.io.Resources;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 2/2/18
 * Time: 4:46 PM
 * Project: munch-data
 */
@Singleton
public final class FoodTagDatabase {
    private final Map<String, Pattern> patternMap = new HashMap<>();

    @Inject
    public FoodTagDatabase() throws IOException {
        URL resource = Resources.getResource("food.txt");
        Resources.readLines(resource, Charset.defaultCharset()).forEach(s -> {
            String food = s.toLowerCase();
            patternMap.put(food, Pattern.compile("(\\b|\\s|^)" + food + "(\\b|\\s|$)"));
        });
    }

    public Map<String, Integer> matches(String text) {
        Map<String, Integer> map = new HashMap<>();
        patternMap.forEach((tag, pattern) -> {
            int matches = count(pattern, text);
            if (matches > 0) {
                map.put(tag, matches);
            }
        });
        return map;
    }

    public Map<String, Integer> matches(List<String> texts) {
        Map<String, Integer> map = new HashMap<>();
        patternMap.forEach((tag, pattern) -> {
            int matches = texts.stream()
                    .mapToInt(text -> count(pattern, text))
                    .sum();
            if (matches > 0) {
                map.put(tag, matches);
            }
        });
        return map;
    }

    private int count(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) count++;
        return count;
    }
}
