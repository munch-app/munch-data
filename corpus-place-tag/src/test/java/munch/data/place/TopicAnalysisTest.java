package munch.data.place;

import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.data.CatalystClient;
import corpus.data.CorpusData;
import munch.data.place.text.CollectedText;
import munch.data.place.text.TextCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 1/2/18
 * Time: 8:53 PM
 * Project: munch-data
 */
class TopicAnalysisTest {

    TextCollector textCollector;
    TopicAnalysis topicAnalysis;
    CatalystClient catalystClient;

    @BeforeEach
    void setUp() {
        System.setProperty("services.corpus.data.url", "http://proxy.corpus.munch.space:8200");

        Injector injector = Guice.createInjector(new PlaceTagModule());
        this.textCollector = injector.getInstance(TextCollector.class);
        this.topicAnalysis = injector.getInstance(TopicAnalysis.class);
        this.catalystClient = injector.getInstance(CatalystClient.class);
    }

    @Test
    void placeAntoinette() throws IOException {
        print(analysis("8759e8cb-a52e-40e4-b75c-a65c9b089f23"));
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

    private List<Map<String, Integer>> analysis(String placeId) throws IOException {
        List<CorpusData> dataList = new ArrayList<>();
        catalystClient.listCorpus(placeId).forEachRemaining(dataList::add);

        List<CollectedText> collectedTexts = textCollector.collect(placeId, dataList);
        List<String> texts = collectedTexts.stream()
                .flatMap(collected -> collected.getTexts().stream())
                .collect(Collectors.toList());
        return topicAnalysis.apply(texts, 5, 30);
    }
}