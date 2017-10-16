package munch.data.place;

import com.google.common.collect.ImmutableList;
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

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;

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

    @Provides
    @Singleton
    @Named("place.priority")
    List<String> providePriorityNames(Config config) {
        return ImmutableList.copyOf(config.getStringList("place.priority"));
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
