package munch.data.place.amalgamate;

import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.CorpusModule;
import corpus.data.CatalystClient;
import corpus.data.CorpusClient;
import corpus.data.CorpusData;
import corpus.data.DataModule;
import corpus.field.PlaceKey;
import munch.data.place.elastic.ElasticModule;
import munch.data.place.matcher.NameMatcher;
import munch.data.place.matcher.SpatialMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by: Fuxing
 * Date: 8/12/2017
 * Time: 6:10 AM
 * Project: munch-data
 */
class SpatialAmalgamateTest {

    NameMatcher nameMatcher;
    SpatialMatcher spatialMatcher;

    SpatialClient spatialClient;

    CorpusClient corpusClient;
    CatalystClient catalystClient;
    Amalgamate amalgamate;

    @BeforeEach
    void setUp() throws IOException {
        System.setProperty("services.corpus.data.url", "http://proxy.corpus.munch.space:8200");
        System.setProperty("services.elastic.url", "http://localhost:9200");
        Injector injector = Guice.createInjector(new ElasticModule(), new CorpusModule(), new DataModule());

        corpusClient = injector.getInstance(CorpusClient.class);
        catalystClient = injector.getInstance(CatalystClient.class);
        nameMatcher = injector.getInstance(NameMatcher.class);
        spatialMatcher = injector.getInstance(SpatialMatcher.class);
        spatialClient = injector.getInstance(SpatialClient.class);

        amalgamate = injector.getInstance(Amalgamate.class);
    }

    @Test
    void maintain() throws Exception {
        CorpusData placeData = corpusClient.get("Sg.Munch.Place", "a5add7ff-1e0f-4061-a5dd-34ef070a6059");
        amalgamate.maintain(placeData);
    }

    protected Iterator<ElasticPlace> search(CorpusData placeData) {
        // Some Sg.Munch.Place might not have latLng ready for use
        return PlaceKey.Location.latLng.get(placeData)
                .map(CorpusData.Field::getValue)
                .map(latLng -> spatialClient.search(latLng, SpatialMatcher.MAX_DISTANCE))
                .orElse(Collections.emptyIterator());
    }

    @Test
    void search() throws Exception {
        corpusClient.patchCatalystId("Global.Instagram.Location", "289695395", null);
        CorpusData placeData = corpusClient.get("Sg.Munch.Place", "a5add7ff-1e0f-4061-a5dd-34ef070a6059");
        search(placeData).forEachRemaining(System.out::println);
    }
}