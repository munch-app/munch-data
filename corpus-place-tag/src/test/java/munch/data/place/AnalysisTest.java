package munch.data.place;

import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.data.CatalystClient;
import corpus.data.CorpusData;
import munch.data.place.text.CollectedText;
import munch.data.place.text.TextCollector;
import org.junit.jupiter.api.BeforeAll;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 2/2/18
 * Time: 4:55 PM
 * Project: munch-data
 */
public class AnalysisTest {
    static Injector injector;
    static TextCollector textCollector;
    static CatalystClient catalystClient;

    @BeforeAll
    static void beforeAllSetup() {
        System.setProperty("services.corpus.data.url", "http://proxy.corpus.munch.space:8200");

        injector = Guice.createInjector(new PlaceTagModule());
        textCollector = injector.getInstance(TextCollector.class);
        catalystClient = injector.getInstance(CatalystClient.class);
    }

    List<String> getTexts(String placeId) {
        List<CorpusData> dataList = new ArrayList<>();
        catalystClient.listCorpus(placeId).forEachRemaining(dataList::add);

        List<CollectedText> collectedTexts = textCollector.collect(placeId, dataList);
        return collectedTexts.stream()
                .flatMap(collected -> collected.getTexts().stream())
                .collect(Collectors.toList());
    }
}
