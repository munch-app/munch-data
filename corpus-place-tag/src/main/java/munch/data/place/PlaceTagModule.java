package munch.data.place;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.CorpusModule;
import corpus.data.DataModule;
import munch.data.dynamodb.DynamoModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 3:21 PM
 * Project: munch-data
 */
public final class PlaceTagModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(PlaceTagModule.class);

    @Override
    protected void configure() {
        install(new CorpusModule());
        install(new DataModule());
        install(new DynamoModule());
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new PlaceTagModule());
        injector.getInstance(PlaceImageCorpus.class).run();
    }
}
