package munch.search;

import com.fasterxml.jackson.databind.JsonNode;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.cluster.Health;
import munch.restful.server.JsonCall;
import munch.restful.server.JsonService;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

/**
 * Created by: Fuxing
 * Date: 20/8/2017
 * Time: 8:53 AM
 * Project: munch-core
 */
@Singleton
public final class HealthService implements JsonService {

    private final JestClient client;

    @Inject
    public HealthService(JestClient client) {
        this.client = client;
    }

    @Override
    public void route() {
        GET("/health/check", this::check);
    }

    private JsonNode check(JsonCall call) throws IOException {
        JestResult result = client.execute(new Health.Builder().build());
        String status = result.getJsonObject().get("status").getAsString();
        if (StringUtils.equalsAnyIgnoreCase(status, "yellow", "green")) {
            return Meta200;
        } else {
            return nodes(503, status);
        }
    }
}
