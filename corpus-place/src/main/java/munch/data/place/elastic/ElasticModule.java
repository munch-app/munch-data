package munch.data.place.elastic;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import munch.restful.WaitFor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.time.Duration;

/**
 * Created by: Fuxing
 * Date: 9/10/2017
 * Time: 2:04 AM
 * Project: munch-data
 */
public final class ElasticModule extends AbstractModule {

    @Override
    protected void configure() {
        requestInjection(this);
    }

    @Inject
    void configureMapping(ElasticMapping mapping) throws IOException {
        mapping.tryCreate();
    }

    @Provides
    @Singleton
    JestClientFactory provideJestFactory() {
        return new JestClientFactory();
    }

    /**
     * Wait for elastic to be started
     *
     * @param config config
     * @return elastic RestClient
     */
    @Provides
    @Singleton
    JestClient provideClient(Config config, JestClientFactory factory) throws InterruptedException {
        String url = config.getString("services.elastic.url");
        WaitFor.host(url, Duration.ofSeconds(180));

        factory.setHttpClientConfig(new HttpClientConfig.Builder(url)
                .multiThreaded(true)
                .defaultMaxTotalConnectionPerRoute(5)
                .readTimeout(30000)
                .connTimeout(15000)
                .build());
        return factory.getObject();
    }
}
