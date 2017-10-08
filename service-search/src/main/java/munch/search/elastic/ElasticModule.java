package munch.search.elastic;

import com.amazonaws.auth.ContainerCredentialsProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import munch.restful.WaitFor;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import vc.inreach.aws.request.AWSSigner;
import vc.inreach.aws.request.AWSSigningRequestInterceptor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Created By: Fuxing Loh
 * Date: 9/3/2017
 * Time: 3:49 PM
 * Project: munch-core
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
    JestClientFactory provideJestFactory(Config config) {
        if (!config.getBoolean("aws.elastic.signing")) {
            return new JestClientFactory();
        }

        AWSSigner awsSigner = new AWSSigner(new ContainerCredentialsProvider(),
                config.getString("aws.elastic.region"), "es",
                () -> LocalDateTime.now(ZoneOffset.UTC));
        AWSSigningRequestInterceptor requestInterceptor = new AWSSigningRequestInterceptor(awsSigner);
        return new JestClientFactory() {
            @Override
            protected HttpClientBuilder configureHttpClient(HttpClientBuilder builder) {
                builder.addInterceptorLast(requestInterceptor);
                return builder;
            }

            @Override
            protected HttpAsyncClientBuilder configureHttpClient(HttpAsyncClientBuilder builder) {
                builder.addInterceptorLast(requestInterceptor);
                return builder;
            }
        };
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
