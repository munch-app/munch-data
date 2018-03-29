package munch.data.place.elastic;

import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.indices.CreateIndex;
import munch.restful.WaitFor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Duration;

/**
 * Created by: Fuxing
 * Date: 9/10/2017
 * Time: 2:04 AM
 * Project: munch-data
 */
public final class ElasticModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(ElasticModule.class);

    @Override
    protected void configure() {
        requestInjection(this);
    }

    @Inject
    void configureMapping(@Named("munch.data.place.jest") JestClient client) throws IOException, InterruptedException {
        Thread.sleep(1500);

        logger.info("Creating index");
        createIndex(client, "postal");
        createIndex(client, "spatial");

        Thread.sleep(3000);
    }

    @Inject
    void configureShutdown(@Named("munch.data.place.jest") JestClient client) {
        Runtime.getRuntime().addShutdownHook(new Thread(client::shutdownClient));
    }

    private static void createIndex(JestClient client, String indexName) throws IOException {
        URL url = Resources.getResource("search-index.json");
        String json = Resources.toString(url, Charset.forName("UTF-8"));
        JestResult result = client.execute(new CreateIndex.Builder(indexName).settings(json).build());
        logger.info("Created index result: {}", result.getJsonString());
    }

    /**
     * Wait for elastic to be started
     *
     * @param config config
     * @return elastic RestClient
     */
    @Provides
    @Singleton
    @Named("munch.data.place.jest")
    JestClient provideClient(Config config) {
        String url = config.getString("services.elastic.url");
        WaitFor.host(url, Duration.ofSeconds(180));

        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder(url)
                .multiThreaded(true)
                .defaultMaxTotalConnectionPerRoute(5)
                .readTimeout(30000)
                .connTimeout(15000)
                .build());
        return factory.getObject();
    }
}
