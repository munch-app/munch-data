package munch.data.place.parser.location;

import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.CorpusModule;
import corpus.data.CorpusClient;
import corpus.data.DataModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 21/10/2017
 * Time: 3:34 PM
 * Project: munch-data
 */
class LocationDatabaseTest {

    LocationDatabase locationDatabase;

    @BeforeEach
    void setUp() {
        System.setProperty("services.corpus.data.url", "http://proxy.corpus.munch.space:8200");

        Injector injector = Guice.createInjector(new DataModule(), new CorpusModule());
        locationDatabase = new LocationDatabase(injector.getInstance(CorpusClient.class));
    }

    @Test
    void findTags() throws Exception {
        Set<String> tags = locationDatabase.findTags(1.3504, 103.8488);
        System.out.println(tags);
    }
}