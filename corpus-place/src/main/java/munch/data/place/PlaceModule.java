package munch.data.place;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.CorpusModule;
import corpus.data.DataModule;
import corpus.engine.EngineGroup;
import munch.data.dynamodb.DynamoModule;
import munch.data.place.elastic.ElasticModule;
import munch.data.place.parser.location.LocationParserModule;

/**
 * Created By: Fuxing Loh
 * Date: 8/10/2017
 * Time: 7:39 PM
 * Project: munch-data
 */
public class PlaceModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new ElasticModule());
        install(new CorpusModule());
        install(new DataModule());
        install(new DynamoModule());
        install(new munch.data.elastic.ElasticModule());

        install(new LocationParserModule());
    }

    public static void main(String[] args) throws InterruptedException {
        Injector injector = Guice.createInjector(new PlaceModule());

        // Start the following corpus
        EngineGroup.start(
                injector.getInstance(ElasticPostalCorpus.class),
                injector.getInstance(ElasticSpatialCorpus.class),
                injector.getInstance(SeedCorpus.class),
                injector.getInstance(PlaceCorpus.class)
        );
        System.exit(0);
    }
}
