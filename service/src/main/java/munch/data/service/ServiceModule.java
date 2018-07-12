package munch.data.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import munch.data.elastic.ElasticModule;
import munch.restful.server.RestfulServer;

import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 30/5/18
 * Time: 2:43 PM
 * Project: munch-data
 */
public final class ServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new ElasticModule());
    }

    @Provides
    @Singleton
    DynamoDB provideDynamoDB() {
        Config config = ConfigFactory.load().getConfig("services.dynamodb");
        AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.defaultClient();

        // If has url mean testing environment is enabled
        if (config.hasPath("url")) {
            String url = config.getString("url");
            dynamoDB = AmazonDynamoDBClientBuilder.standard()
                    .withEndpointConfiguration(
                            new AwsClientBuilder.EndpointConfiguration(url, "us-west-2"))
                    .withCredentials(
                            new AWSStaticCredentialsProvider(
                                    new BasicAWSCredentials("foo", "bar")))
                    .build();
        }

        return new DynamoDB(dynamoDB);
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new ServiceModule());
        RestfulServer.start("/v4.0",
                injector.getInstance(PlaceService.class),
                injector.getInstance(TagService.class),
                injector.getInstance(LandmarkService.class),
                injector.getInstance(AreaService.class),
                injector.getInstance(ElasticService.class),
                injector.getInstance(BrandService.class)
        ).withHealth();
    }
}
