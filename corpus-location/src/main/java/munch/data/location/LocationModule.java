package munch.data.location;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.CorpusModule;
import corpus.data.DataModule;
import corpus.engine.EngineGroup;
import munch.data.dynamodb.DynamoModule;
import munch.data.elastic.ElasticModule;

/**
 * Created by: Fuxing
 * Date: 10/10/2017
 * Time: 3:20 AM
 * Project: munch-data
 */
public class LocationModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new CorpusModule());
        install(new DataModule());
        install(new DynamoModule());
        install(new ElasticModule());
    }

    public static void main(String[] args) throws InterruptedException {
        Injector injector = Guice.createInjector(new LocationModule());

        // Start the following corpus
        EngineGroup.start(
                injector.getInstance(SeedingCorpus.class),
                injector.getInstance(LocationCorpus.class)
        );
    }
}
