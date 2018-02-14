package munch.data.place;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.CorpusModule;
import corpus.data.DataModule;
import corpus.engine.EngineGroup;
import munch.data.dynamodb.DynamoModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by: Fuxing
 * Date: 14/2/18
 * Time: 7:33 PM
 * Project: munch-data
 */
public class PlaceTrackingModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(PlaceTrackingModule.class);

    @Override
    protected void configure() {
        install(new CorpusModule());
        install(new DataModule());
        install(new DynamoModule());
    }

    public static void main(String[] args) throws InterruptedException {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            logger.error("Uncaught Exceptions: ", e.getCause());
            System.exit(0);
        });

        Injector injector = Guice.createInjector(new PlaceTrackingModule());
        EngineGroup.start(
                injector.getInstance(PlaceTrackingCorpus.class)
        );
    }
}
