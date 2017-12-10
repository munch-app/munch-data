package munch.data.container;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.CorpusModule;
import corpus.data.DataModule;
import corpus.engine.EngineGroup;
import munch.data.container.matcher.ContainerPlaceCatalyst;
import munch.data.dynamodb.DynamoModule;
import munch.data.elastic.ElasticModule;

/**
 * Created by: Fuxing
 * Date: 10/12/2017
 * Time: 9:49 AM
 * Project: munch-data
 */
public class ContainerModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new CorpusModule());
        install(new DataModule());
        install(new DynamoModule());
        install(new ElasticModule());
    }

    public static void main(String[] args) throws InterruptedException {
        Injector injector = Guice.createInjector(new ContainerModule());

        // Start the following corpus
        EngineGroup.start(
                injector.getInstance(SeedingCorpus.class),
                injector.getInstance(ContainerCorpus.class),
                injector.getInstance(ContainerPlaceCatalyst.class)
        );
        System.exit(0);
    }
}
