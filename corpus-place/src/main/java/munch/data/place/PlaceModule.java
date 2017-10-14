package munch.data.place;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import corpus.CorpusModule;
import corpus.data.DataModule;
import corpus.engine.EngineGroup;
import munch.data.place.elastic.ElasticModule;
import munch.data.place.parser.location.StreetNameModule;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Set;

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

        install(new StreetNameModule());
    }

    @Provides
    @Singleton
    @Named("place.seeds")
    Set<String> provideSeedNames(Config config) {
        return ImmutableSet.copyOf(config.getStringList("place.seeds"));
    }

    @Provides
    @Singleton
    @Named("place.trees")
    Set<String> provideTreeNames(Config config) {
        return ImmutableSet.copyOf(config.getStringList("place.trees"));
    }

    public static void main(String[] args) throws InterruptedException {
        Injector injector = Guice.createInjector(new PlaceModule());

        // Start the following corpus
        EngineGroup.start(
                injector.getInstance(ElasticCorpus.class),
                injector.getInstance(SeedCorpus.class),
                injector.getInstance(TreeCorpus.class)
        );
    }
}
