package munch.collections;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

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

    @Provides
    @Singleton
    DynamoDB provideDynamoDB(AmazonDynamoDB client) {
        return new DynamoDB(client);
    }

    @Provides
    @Singleton
    AmazonDynamoDB provideClient() {
        AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClient.builder();
        builder.withCredentials(new DefaultAWSCredentialsProviderChain());
        builder.withRegion(Regions.AP_SOUTHEAST_1);
        return builder.build();
    }
}
