package munch.data.clients;

import com.google.inject.AbstractModule;
import com.mashape.unirest.http.Unirest;
import com.typesafe.config.Config;
import munch.restful.WaitFor;

import javax.inject.Inject;
import java.time.Duration;

/**
 * In theses clients: catalystId is also know also placeId
 * <p>
 * Created by: Fuxing
 * Date: 15/4/2017
 * Time: 3:44 AM
 * Project: munch-core
 */
public class ClientModule extends AbstractModule {

    @Override
    protected void configure() {
        Unirest.setTimeouts(60000, 60000);
        requestInjection(this);
    }

    @Inject
    void waitFor(Config config) {
        WaitFor.host(config.getString("services.data.url"), Duration.ofSeconds(180));
        WaitFor.host(config.getString("services.elastic.url"), Duration.ofSeconds(180));
    }
}
