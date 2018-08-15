package munch.data.service;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import munch.data.aws.AWSDynamoModule;
import munch.data.elastic.ElasticModule;
import munch.restful.server.RestfulServer;

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
        install(new AWSDynamoModule());
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new ServiceModule());
        RestfulServer.start("/v4.0",
                injector.getInstance(PlaceService.class),
                injector.getInstance(PlaceAwardService.class),
                injector.getInstance(TagService.class),
                injector.getInstance(LandmarkService.class),
                injector.getInstance(AreaService.class),
                injector.getInstance(ElasticService.class),
                injector.getInstance(BrandService.class)
        ).withHealth();
    }
}
