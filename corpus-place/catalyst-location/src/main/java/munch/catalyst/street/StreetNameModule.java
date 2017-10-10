package munch.catalyst.street;

import catalyst.utils.exception.Retriable;
import catalyst.utils.exception.SleepRetriable;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.typesafe.config.Config;
import fr.dudie.nominatim.client.JsonNominatimClient;
import fr.dudie.nominatim.client.NominatimClient;
import munch.restful.WaitFor;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Created by: Fuxing
 * Date: 4/5/2017
 * Time: 10:06 PM
 * Project: munch-corpus
 */
public class StreetNameModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(StreetNameModule.class);

    @Provides
    NominatimClient provideClient(Config config) throws UnirestException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        String url = config.getString("nominatim.url");
        String email = config.getString("nominatim.email");

        waitForNomination(url);
        return new JsonNominatimClient(url, httpClient, email);
    }

    /**
     * Wait for 200 seconds for nominatim to be ready
     *
     * @param url url of nominatim service
     */
    private void waitForNomination(String url) throws UnirestException {
        WaitFor.host(url, Duration.ofSeconds(200));
        Retriable retriable = new SleepRetriable(15, Duration.ofSeconds(20), (throwable, integer) -> {
            logger.info("Waiting for {} to be ready.", url);
        });
        retriable.loop(() -> new GetRequest(HttpMethod.GET,
                url + "/reverse?format=json").asJson().getBody());
    }

    @Override
    protected void configure() {

    }
}
