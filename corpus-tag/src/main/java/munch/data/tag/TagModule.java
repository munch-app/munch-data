package munch.data.tag;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import corpus.CorpusModule;
import corpus.data.DataModule;
import corpus.engine.EngineGroup;

/**
 * Created by: Fuxing
 * Date: 10/10/2017
 * Time: 3:20 AM
 * Project: munch-data
 */
public class TagModule extends AbstractModule{

    @Override
    protected void configure() {
        install(new CorpusModule());
        install(new DataModule());
    }

    public static void main(String[] args) throws InterruptedException {
        Injector injector = Guice.createInjector(new TagModule());

        // Start the following corpus
        EngineGroup.start(
                injector.getInstance(SyncCorpus.class),
                injector.getInstance(TagCorpus.class)
        );
    }
}
