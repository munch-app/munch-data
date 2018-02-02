package munch.data.place;

import munch.data.place.popular.FoodTagDatabase;
import munch.data.place.popular.PopularFoodParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 2/2/18
 * Time: 4:54 PM
 * Project: munch-data
 */
public class PopularAnalysisTest extends AnalysisTest {

    FoodTagDatabase foodTagDatabase;
    PopularFoodParser foodParser;

    @BeforeEach
    void setUp() {
        foodTagDatabase = injector.getInstance(FoodTagDatabase.class);
        foodParser = injector.getInstance(PopularFoodParser.class);
    }

    @Test
    void placeAntoinette() throws IOException {
        List<String> texts = getTexts("8759e8cb-a52e-40e4-b75c-a65c9b089f23");
        foodParser.parse(texts, 100).forEach((s, integer) -> {
            System.out.println(s + ": " + integer);
        });
    }

    @Test
    void wordTest() {
        foodParser.parse(List.of("food like white chocolate", "purple chocolate.", "chocolate", "chocolate", "chocolate cake", "food", "white chocolate is the"), 100).forEach((s, integer) -> {
            System.out.println(s + ": " + integer);
        });
    }
}
