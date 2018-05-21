package munch.data.place.parser;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.CorpusModule;
import corpus.data.CatalystClient;
import corpus.data.CorpusData;
import corpus.data.DataModule;
import munch.data.structure.Place;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * Created by: Fuxing
 * Date: 22/5/18
 * Time: 1:39 AM
 * Project: munch-data
 */
class WebsiteParserTest {

    WebsiteParser parser;
    CatalystClient catalystClient;

    @BeforeEach
    void setUp() {
        System.setProperty("services.corpus.data.url", "http://proxy.corpus.munch.space:8200");

        Injector injector = Guice.createInjector(new DataModule(), new CorpusModule());
        parser = injector.getInstance(WebsiteParser.class);
        catalystClient = injector.getInstance(CatalystClient.class);
    }

    @Test
    void website() {
        ArrayList<CorpusData> dataList = Lists.newArrayList(catalystClient.listCorpus("aa53703d-b468-4a46-b448-30f392d32072"));
        String website = parser.parse(new Place(), dataList);
        System.out.println(website);
    }
}