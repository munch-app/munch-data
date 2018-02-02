package munch.data.place;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import corpus.CorpusModule;
import corpus.data.DataModule;
import corpus.engine.EngineGroup;
import io.searchbox.client.JestClient;
import munch.data.dynamodb.DynamoModule;
import munch.data.place.elastic.ElasticModule;
import munch.data.place.parser.location.LocationParserModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created By: Fuxing Loh
 * Date: 8/10/2017
 * Time: 7:39 PM
 * Project: munch-data
 */
public class PlaceModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(PlaceModule.class);

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

        injector.getInstance(JestClient.class).shutdownClient();
        injector.getInstance(Key.get(JestClient.class, Names.named("munch.data.place.jest"))).shutdownClient();
        com.amazonaws.http.IdleConnectionReaper.shutdown();
        logger.info("Corpus should shutdown.");

        Thread.getAllStackTraces().forEach((thread, stackTraceElements) -> {
            logger.error("Thread: {} {}", thread.getName(), Arrays.toString(stackTraceElements));
        });
        System.exit(0);
    }
}
