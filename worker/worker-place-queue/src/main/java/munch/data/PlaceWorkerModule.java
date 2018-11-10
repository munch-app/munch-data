package munch.data;

import catalyst.aws.AWSQueueModule;
import catalyst.utils.health.HealthCheckServer;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Created by: Fuxing
 * Date: 9/11/18
 * Time: 6:47 PM
 * Project: munch-data
 */
public final class PlaceWorkerModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new AWSQueueModule());
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new PlaceWorkerModule());
        HealthCheckServer.startBlocking(
                injector.getInstance(PlaceWorker.class)
        );
    }
}
