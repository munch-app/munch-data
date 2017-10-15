package munch.data.dynamodb;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import munch.data.clients.PlaceCardClient;
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
        if (!config.hasPath("services.munch-data.dynamodb.url")) return;

        CreateTableUtils utils = new CreateTableUtils(dynamoDB);
        utils.createTable(PlaceClient.DYNAMO_TABLE_NAME, "_id");
        utils.createTable(PlaceCardClient.DYNAMO_TABLE_NAME, "_placeId", "_cardName");
    }

    @Provides
    @Singleton
    DynamoDB provideDynamoDB(AmazonDynamoDB client) {
        return new DynamoDB(client);
    }

    @Provides
    @Singleton
    AmazonDynamoDB provideClient(Config config) {
        String region = config.getString("services.munch-data.dynamodb.aws.region");

        AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClient.builder();
        builder.withCredentials(new DefaultAWSCredentialsProviderChain());

        // Endpoint If Exist (Dev Mode)
        if (config.hasPath("services.munch-data.dynamodb.url")) {
            String endpoint = config.getString("services.munch-data.dynamodb.url");
            builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region));
        } else {
            builder.withRegion(region);
        }

        return builder.build();
    }
}
