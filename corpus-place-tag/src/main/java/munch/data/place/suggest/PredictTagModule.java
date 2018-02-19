package munch.data.place.suggest;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import munch.restful.WaitFor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;

/**
 * Created by: Fuxing
 * Date: 19/2/2018
 * Time: 9:05 PM
 * Project: munch-data
 */
public class PredictTagModule extends AbstractModule {

    @Override
    protected void configure() {
        requestInjection(this);
    }

    @Inject
    void waitFor(Config config) {
        WaitFor.host(config.getString("services.tag-predict.url"), Duration.ofMinutes(2));
    }

    @Provides
    @Singleton
    PredictTagClient provideTagClient(Config config) {
        return new PredictTagClient(config.getString("services.tag-predict.url"));
    }
}
