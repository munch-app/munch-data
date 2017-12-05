package munch.data.place;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.CorpusModule;
import corpus.data.DataModule;
import corpus.engine.EngineGroup;
import munch.data.place.elastic.ElasticModule;

/**
 * Created by: Fuxing
 * Date: 17/10/2017
 * Time: 3:25 AM
 * Project: munch-data
 */
public class ElasticCorpusTest extends AbstractModule {

    @Override
    protected void configure() {
        install(new ElasticModule());
        install(new CorpusModule());
        install(new DataModule());
    }

    public static void main(String[] args) throws InterruptedException {
        System.setProperty("services.corpus.data.url", "http://proxy.corpus.munch.space:8200");
        System.setProperty("services.elastic.url", "http://localhost:9200");

        Injector injector = Guice.createInjector(new ElasticCorpusTest());

        // Start the following corpus
        EngineGroup.start(
                injector.getInstance(ElasticCorpus.class)
        );
        System.exit(0);
    }
}
