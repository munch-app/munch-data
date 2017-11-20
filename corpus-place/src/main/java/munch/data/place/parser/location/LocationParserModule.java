package munch.data.place.parser.location;

import catalyst.utils.exception.Retriable;
import catalyst.utils.exception.SleepRetriable;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.typesafe.config.Config;
import corpus.location.GeocodeClient;
import fr.dudie.nominatim.client.JsonNominatimClient;
import fr.dudie.nominatim.client.NominatimClient;
import munch.restful.WaitFor;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;

/**
 * Created by: Fuxing
 * Date: 4/5/2017
 * Time: 10:06 PM
 * Project: munch-corpus
 */
public class LocationParserModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(LocationParserModule.class);

    @Override
    protected void configure() {
        // OneMapApi, uses TLSv1.2
        System.setProperty("https.protocols", "TLSv1.2");

        requestInjection(this);
    }

    /**
     * Wait for 200 seconds for nominatim to be ready
     *
     * @param config to read url from
     */
    @Inject
    void waitForNomination(Config config) throws UnirestException {
        String url = config.getString("services.nominatim.url");

        WaitFor.host(url, Duration.ofSeconds(200));
        Retriable retriable = new SleepRetriable(15, Duration.ofSeconds(20), (throwable, integer) -> {
            logger.info("Waiting for {} to be ready.", url);
        });
        retriable.loop(() -> new GetRequest(HttpMethod.GET,
                url + "/reverse?format=json").asJson().getBody());
    }

    @Provides
    @Singleton
    NominatimClient provideClient(Config config) throws UnirestException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        String url = config.getString("services.nominatim.url");
        String email = config.getString("services.nominatim.email");

        return new JsonNominatimClient(url, httpClient, email);
    }

    @Provides
    @Singleton
    GeocodeClient provideGeocodeClient(Config config) {
        WaitFor.host(config.getString("services.location.url"), Duration.ofSeconds(300));
        return new GeocodeClient(config.getString("services.location.url"));
    }

    @Provides
    @Singleton
    GeocodeApi provideGeocodeApi(GeoPostcodesApi geoPostcodes, OneMapApi oneMapApi) {
        return new GeocodeApi.Chain(geoPostcodes, oneMapApi);
    }
}
