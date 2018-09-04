package munch.data.elastic;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import munch.restful.WaitFor;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(ElasticModule.class);

    @Override
    protected void configure() {
        requestInjection(this);
    }

    @Inject
    void configureMapping(ElasticMapping mapping) throws IOException {
        try {
            mapping.tryCreate();
        } catch (Exception e) {
            logger.error("ElasticMapping Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Inject
    void configShutdownHook(JestClient jestClient) {
        Runtime.getRuntime().addShutdownHook(new Thread(jestClient::shutdownClient));
    }

    @Provides
    @Singleton
    JestClientFactory provideJestFactory() {
        Config config = ConfigFactory.load();

        if (config.getBoolean("services.elastic.production")) {
            AWSSigner awsSigner = new AWSSigner(new DefaultAWSCredentialsProviderChain(),
                    config.getString("services.elastic.aws.region"), "es",
                    () -> LocalDateTime.now(ZoneOffset.UTC)
            );

            return new JestClientFactory() {
                AWSSigningRequestInterceptor requestInterceptor = new AWSSigningRequestInterceptor(awsSigner);

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
        } else {
            return new JestClientFactory();
        }
    }


    /**
     * Wait for elastic to be started
     *
     * @return elastic RestClient
     */
    @Provides
    @Singleton
    JestClient provideClient(JestClientFactory factory) {
        Config config = ConfigFactory.load();
        String url = config.getString("services.elastic.url");

        if (config.getBoolean("services.elastic.production")) {
            WaitFor.host(url, Duration.ofSeconds(180));
        } else {
            WaitFor.statusOk(url, Duration.ofSeconds(180));
        }

        factory.setHttpClientConfig(new HttpClientConfig.Builder(url)
                .multiThreaded(true)
                .defaultMaxTotalConnectionPerRoute(5)
                .readTimeout(30000)
                .connTimeout(15000)
                .build());

        return factory.getObject();
    }
}
