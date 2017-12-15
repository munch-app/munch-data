package munch.data.place.processor;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import munch.finn.FinnClient;
import munch.restful.WaitFor;

import javax.inject.Singleton;
import java.time.Duration;

/**
 * Created by: Fuxing
 * Date: 15/12/2017
 * Time: 10:18 AM
 * Project: munch-data
 */
public class ProcessorModule extends AbstractModule {

    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    FinnClient provideFinnClient(Config config) {
        String finnUrl = config.getString("services.finn.url");
        WaitFor.host(finnUrl, Duration.ofMinutes(2));
        return new FinnClient(finnUrl);
    }
}
