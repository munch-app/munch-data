package munch.data.place.popular;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 2/2/18
 * Time: 5:05 PM
 * Project: munch-data
 */
@Singleton
public final class PopularFoodParser {
    private final FoodTagDatabase tagDatabase;
    private final FoodStopDatabase foodStopDatabase;

    @Inject
    public PopularFoodParser(FoodTagDatabase tagDatabase, FoodStopDatabase foodStopDatabase) {
        this.tagDatabase = tagDatabase;
        this.foodStopDatabase = foodStopDatabase;
    }

    public Map<String, Integer> parse(List<String> texts, int limit) {
        Map<String, Integer> matches = tagDatabase.matches(texts);
        return matches.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .filter(entry -> !foodStopDatabase.is(entry.getKey()))
                .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }
}
