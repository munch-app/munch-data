package munch.data.dynamodb;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import munch.data.clients.PlaceClient;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 10/10/17
 * Time: 7:42 PM
 * Project: munch-data
 */
public final class DynamoModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    @Inject
    void setupTable(Config config, AmazonDynamoDB dynamoDB) throws InterruptedException {
        // Is in production mode, don't need setup table
        if (!config.hasPath("services.dynamodb.url")) return;

        CreateTableRequest request = new CreateTableRequest();
        request.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L));
        request.setTableName(PlaceClient.DYNAMO_TABLE_NAME);

        request.setAttributeDefinitions(ImmutableList.of(
                new AttributeDefinition("_id", ScalarAttributeType.S))
        );

        request.setKeySchema(ImmutableList.of(
                new KeySchemaElement("_id", KeyType.HASH))
        );

        // Create table if not already exists & wait
        TableUtils.createTableIfNotExists(dynamoDB, request);
        TableUtils.waitUntilActive(dynamoDB, PlaceClient.DYNAMO_TABLE_NAME);
    }

    @Provides
    @Singleton
    DynamoDB provideDynamoDB(AmazonDynamoDB client) {
        return new DynamoDB(client);
    }

    @Provides
    @Singleton
    AmazonDynamoDB provideClient(Config config) {
        String region = config.getString("services.dynamodb.aws.region");

        AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClient.builder();
        builder.withCredentials(new DefaultAWSCredentialsProviderChain());

        // Endpoint If Exist (Dev Mode)
        if (config.hasPath("services.dynamodb.url")) {
            String endpoint = config.getString("services.dynamodb.url");
            builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region));
        } else {
            builder.withRegion(region);
        }

        return builder.build();
    }
}
