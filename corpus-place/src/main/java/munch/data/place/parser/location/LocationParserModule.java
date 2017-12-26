package munch.data.place.parser.location;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import corpus.location.LocationClient;
import munch.restful.WaitFor;

import javax.inject.Singleton;
import java.time.Duration;

/**
 * Created by: Fuxing
 * Date: 4/5/2017
 * Time: 10:06 PM
 * Project: munch-corpus
 */
public class LocationParserModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    LocationClient provideClient(Config config) {
        WaitFor.host(config.getString("services.location.url"), Duration.ofSeconds(300));
        return new LocationClient(config.getString("services.location.url"));
    }
}
