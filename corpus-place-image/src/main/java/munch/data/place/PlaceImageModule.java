package munch.data.place;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.CorpusModule;
import corpus.data.DataModule;
import munch.data.dynamodb.DynamoModule;
import munch.data.place.processor.ProcessorModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 3:21 PM
 * Project: munch-data
 */
public final class PlaceImageModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(PlaceImageModule.class);

    @Override
    protected void configure() {
        install(new CorpusModule());
        install(new DataModule());
        install(new DynamoModule());
        install(new ProcessorModule());
    }

    public static void main(String[] args) {
        try {
            Injector injector = Guice.createInjector(new PlaceImageModule());
            injector.getInstance(PlaceImageCorpus.class).run();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }
}