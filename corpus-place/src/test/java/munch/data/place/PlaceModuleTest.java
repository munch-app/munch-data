package munch.data.place;

import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.engine.EngineGroup;

/**
 * Created by: Fuxing
 * Date: 16/10/2017
 * Time: 7:41 PM
 * Project: munch-data
 */
class PlaceModuleTest {

    public static void main(String[] args) throws InterruptedException {
        System.setProperty("services.corpus.data.url", "http://proxy.corpus.munch.space:8200");

        Injector injector = Guice.createInjector(new PlaceModule());

        // Start the following corpus
        EngineGroup.start(
                injector.getInstance(ElasticCorpus.class),
                injector.getInstance(SeedCorpus.class),
                injector.getInstance(PlaceCorpus.class)
        );
    }
}