package munch.data.place.parser.location;

import catalyst.utils.LatLngUtils;
import catalyst.utils.exception.ExceptionRetriable;
import catalyst.utils.exception.Retriable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import munch.restful.client.ExceptionParser;
import munch.restful.core.JsonUtils;
import munch.restful.core.exception.OfflineException;
import munch.restful.core.exception.TimeoutException;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Created by: Fuxing
 * Date: 25/8/2017
 * Time: 12:36 PM
 * Project: munch-corpus
 */
@Singleton
public final class OneMapApi {
    private final Retriable retriable = new ExceptionRetriable(50, Duration.ofMinutes(5),
            OfflineException.class, TimeoutException.class);
    private final ObjectMapper objectMapper = JsonUtils.objectMapper;

    private int callsInMinute;

    @Nullable
    public LatLngUtils.LatLng geocode(String postal) {
        if (StringUtils.isBlank(postal)) return null;
        postal = postal.length() == 5 ? "0" + postal : postal;

        try {
            JsonNode rootNode = search(postal);
            JsonNode result = rootNode.path("results").path(0);

            if (!result.isMissingNode()) {
                return new LatLngUtils.LatLng(
                        result.path("LATITUDE").asDouble(),
                        result.path("LONGITUDE").asDouble()
                );
            }

            return null;
        } catch (Exception exception) {
            ExceptionParser.parse(exception);
            throw new RuntimeException(exception);
        }
    }

    /**
     * Note that this method is rated at 250 calls per min.
     * Every 250 call it will sleep for 1 minute
     * <p>
     * <p> Source:
     * https://docs.onemap.sg/#introduction
     * Please note that we have set a maximum of 250 calls per min.
     * If you wish to increase your limit, contact onemap@sla.gov.sg
     *
     * @param query query
     * @return JsonNode of search
     * @throws Exception any exception
     */
    private JsonNode search(String query) throws Exception {
        if (++callsInMinute >= 250) {
            callsInMinute = 0;
            TimeUnit.MINUTES.sleep(1);
        }

        return retriable.loop(() -> {
            HttpResponse<String> response = Unirest.get("https://developers.onemap.sg/commonapi/search")
                    .queryString("searchVal", query)
                    .queryString("returnGeom", "Y")
                    .queryString("getAddrDetails", "N")
                    .asString();

            return objectMapper.readTree(response.getBody());
        });
    }
}
