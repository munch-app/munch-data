package munch.data.service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
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
        return new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new ServiceModule());
        RestfulServer.start("/v4.0",
                injector.getInstance(PlaceService.class),
                injector.getInstance(TagService.class),
                injector.getInstance(LandmarkService.class),
                injector.getInstance(AreaService.class),
                injector.getInstance(ElasticService.class)
        ).withHealth();
    }
}
