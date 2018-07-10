package munch.data.service;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
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
 * Date: 2/7/18
 * Time: 2:39 PM
 * Project: munch-data
 */
public final class TestModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new ElasticModule());
    }

    @Provides
    @Singleton
    DynamoDB provideDynamoDB(AmazonDynamoDB amazonDynamoDB) {
        return new DynamoDB(amazonDynamoDB);
    }

    @Provides
    @Singleton
    AmazonDynamoDB provideAmazonDynamoDB() {
        Config config = ConfigFactory.load().getConfig("services.dynamodb");

        AwsClientBuilder.EndpointConfiguration endpoint =
                new AwsClientBuilder.EndpointConfiguration(config.getString("url"), config.getString("region"));

        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(endpoint)
                .build();
    }

    public static void main(String[] args) throws InterruptedException {
        Injector injector = Guice.createInjector(new TestModule());

        // Setup required table if not found.
        setupTables(injector.getInstance(AmazonDynamoDB.class));

        RestfulServer.start("/v4.0",
                injector.getInstance(LandmarkService.class)
        ).withHealth();
    }

    public static void setupTables(AmazonDynamoDB amazonDynamoDB) throws InterruptedException {
        for (Config config : ConfigFactory.load().getConfigList("persistence.mappings")) {
            String tableName = config.getString("tableName");
            String dataKey = config.getString("dataKey");

            CreateTableRequest request = new CreateTableRequest()
                    .withTableName(tableName)
                    .withProvisionedThroughput(
                            new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(10L)
                    )
                    .withAttributeDefinitions(
                            new AttributeDefinition().withAttributeName(dataKey).withAttributeType(ScalarAttributeType.S)
                    )
                    .withKeySchema(
                            new KeySchemaElement().withAttributeName(dataKey).withKeyType(KeyType.HASH)
                    );

            TableUtils.createTableIfNotExists(amazonDynamoDB, request);
            TableUtils.waitUntilActive(amazonDynamoDB, tableName);
        }
    }
}
