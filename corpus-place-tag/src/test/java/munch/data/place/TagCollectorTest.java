package munch.data.place;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.data.CatalystClient;
import corpus.data.CorpusData;
import munch.data.place.collector.DataCollector;
import munch.data.place.collector.TagCollector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Created by: Fuxing
 * Date: 9/4/2018
 * Time: 6:56 AM
 * Project: munch-data
 */
public class TagCollectorTest {
    static TagCollector collector;
    static CatalystClient catalystClient;

    @BeforeAll
    static void setUp() {
        Injector injector = Guice.createInjector(new DataCollector.CollectModule());
        collector = injector.getInstance(TagCollector.class);
        catalystClient = injector.getInstance(CatalystClient.class);
    }

    @Test
    void placeId() throws Exception {
        String placeId = "7fb0f16b-4a66-4922-8857-28cb5d42d8fe";

        List<CorpusData> dataList = Lists.newArrayList(catalystClient.listCorpus(placeId));
        TagCollector.TagBuilder tagBuilder = collector.collect(placeId, dataList);

        System.out.println(tagBuilder.withTrusted());
        System.out.println(tagBuilder.withAll());
    }
}
