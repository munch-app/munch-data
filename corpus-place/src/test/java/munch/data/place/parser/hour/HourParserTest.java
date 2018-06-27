package munch.data.place.parser.hour;

import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.CorpusModule;
import corpus.data.CatalystClient;
import corpus.data.CorpusData;
import corpus.data.DataModule;
import munch.data.structure.Place;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 26/2/18
 * Time: 2:21 PM
 * Project: munch-data
 */
class HourParserTest {
    private static final Logger logger = LoggerFactory.getLogger(HourParserTest.class);

    CatalystClient catalystClient;
    HourParser hourParser;

    @BeforeEach
    void setUp() {
        System.setProperty("services.corpus.data.url", "http://proxy.corpus.munch.space:8200");

        Injector injector = Guice.createInjector(new DataModule(), new CorpusModule());
        catalystClient = injector.getInstance(CatalystClient.class);
        hourParser = injector.getInstance(HourParser.class);
    }

    private List<CorpusData> collect(String catalystId) {
        List<CorpusData> list = new ArrayList<>();
        catalystClient.listCorpus(catalystId).forEachRemaining(data -> {
            if (!"Sg.Munch.Place".equals(data.getCorpusName())) {
                list.add(data);
            }
        });
        return list;
    }

    @Test
    void kaylee() {
        List<CorpusData> list = collect("8cf093bd-c1c3-43f1-8a51-615ca624a99d");
        List<Place.Hour> hourList = hourParser.parse(new Place(), list);
        System.out.println(hourList);
    }

    @Test
    void wheelersyard() {
        List<CorpusData> list = collect("c948a87d-d05f-4d32-8566-f8e06f9d9dd5");
        List<Place.Hour> hourList = hourParser.parse(new Place(), list);
        System.out.println(hourList);
    }
}