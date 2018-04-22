package munch.data.place.graph;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import corpus.CorpusModule;
import corpus.data.DataModule;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import munch.data.place.graph.matcher.MatcherModule;
import munch.data.place.parser.ParserModule;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 2/4/2018
 * Time: 4:44 PM
 * Project: munch-data
 */
public class PlaceGraphTestModule extends AbstractModule {

    @Override
    protected void configure() {
        System.setProperty("services.corpus.data.url", "http://proxy.corpus.munch.space:8200");
        System.setProperty("services.elastic.url", "http://localhost:9200");
        System.setProperty("services.location.url", "http://localhost:9200");

        install(new CorpusModule());
        install(new DataModule());

        install(new GraphElasticTestModule());
        install(new ParserModule());
        install(new MatcherModule());
    }

    public static class GraphElasticTestModule extends AbstractModule {
        @Provides
        @Singleton
        @Named("munch.data.place.jest")
        JestClient provideClient() {
            JestClientFactory factory = new JestClientFactory();
            factory.setHttpClientConfig(new HttpClientConfig.Builder("http://localhost:9200")
                    .multiThreaded(true)
                    .defaultMaxTotalConnectionPerRoute(5)
                    .readTimeout(30000)
                    .connTimeout(15000)
                    .build());
            return factory.getObject();
        }
    }

    public static Injector getInjector() {
        return Guice.createInjector(new PlaceGraphTestModule());
    }
}
