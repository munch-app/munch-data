package catalyst.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import munch.restful.WaitFor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;

/**
 * Created by: Fuxing
 * Date: 25/7/18
 * Time: 12:22 AM
 * Project: catalyst
 */
public final class AWSQueueModule extends AbstractModule {

    @Override
    protected void configure() {
        Config config = ConfigFactory.load().getConfig("services.sqs");
        if (config.hasPath("url")) {
            requestInjection(this);
        }
    }

    @Inject
    void setup(LocalQueueSetup setup) {
        String url = ConfigFactory.load().getString("services.sqs.url");
        WaitFor.host(url, Duration.ofSeconds(60));
        setup.setup();
    }

    @Provides
    @Singleton
    AmazonSQS provideAmazonSQS() {
        Config config = ConfigFactory.load().getConfig("services.sqs");

        if (config.hasPath("url")) {
            return AmazonSQSClientBuilder.standard()
                    .withEndpointConfiguration(
                            new AwsClientBuilder.EndpointConfiguration(
                                    config.getString("url"), "us-west-2"))
                    .withCredentials(
                            new AWSStaticCredentialsProvider(
                                    new BasicAWSCredentials("foo", "bar")))
                    .build();
        } else {
            return AmazonSQSClientBuilder.defaultClient();
        }
    }
}
