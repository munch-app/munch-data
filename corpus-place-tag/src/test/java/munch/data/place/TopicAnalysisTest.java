package munch.data.place;

import munch.data.place.topic.TopicAnalysis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 1/2/18
 * Time: 8:53 PM
 * Project: munch-data
 */
class TopicAnalysisTest extends AnalysisTest {
    TopicAnalysis topicAnalysis;

    @BeforeEach
    void setUp() {
        topicAnalysis = injector.getInstance(TopicAnalysis.class);
    }

    @Test
    void placeAntoinette() throws IOException {
        List<String> texts = getTexts("8759e8cb-a52e-40e4-b75c-a65c9b089f23");
        print(topicAnalysis.apply(texts, 5, 50));
    }

    private void print(List<Map<String, Integer>> list) {
        list.forEach(stringIntegerMap -> {
            System.out.println("Topic: ");
            stringIntegerMap.forEach((s, integer) -> {
                System.out.println(s + ": " + integer);
            });
            System.out.println("");
        });
    }
}