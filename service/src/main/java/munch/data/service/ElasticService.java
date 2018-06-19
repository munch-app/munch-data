package munch.data.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.searchbox.client.JestClient;
import io.searchbox.core.Count;
import io.searchbox.core.MultiSearch;
import io.searchbox.core.Search;
import munch.data.elastic.ElasticMapping;
import munch.data.exception.ElasticException;
import munch.restful.core.JsonUtils;
import munch.restful.server.JsonCall;
import munch.restful.server.JsonResult;
import munch.restful.server.JsonService;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 31/5/18
 * Time: 5:09 PM
 * Project: munch-data
 */
@Singleton
public final class ElasticService implements JsonService {
    private final JestClient client;

    @Inject
    public ElasticService(JestClient client) {
        this.client = client;
    }

    @Override
    public void route() {
        PATH("/elastic", () -> {
            POST("/search", this::search);
            POST("/search/multi", this::search);
            POST("/count", this::count);
        });
    }

    /**
     * Body:
     * <pre>
     *     {'for search endpoint'}
     * </pre>
     *
     * @param call json call
     * @return {'data': 'elastic result'}
     */
    private JsonResult search(JsonCall call) {
        Search search = new Search.Builder(JsonUtils.toString(call.bodyAsJson()))
                .addIndex(ElasticMapping.INDEX_NAME)
                .build();
        try {
            String json = client.execute(search).getJsonString();
            return result(200, objectMapper.readTree(json));
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }

    /**
     * Body:
     * <pre>
     *     [
     *      'for search endpoint 1',
     *      'for search endpoint 2'
     *     ]
     * </pre>
     *
     * @param call json call
     * @return {'data': ['elastic result 1', 'elastic result 2']}
     */
    private JsonResult searchMulti(JsonCall call) {
        JsonNode arrayNode = call.bodyAsJson();
        List<Search> searches = new ArrayList<>();

        for (JsonNode jsonNode : arrayNode) {
            searches.add(new Search.Builder(JsonUtils.toString(jsonNode))
                    .addIndex(ElasticMapping.INDEX_NAME)
                    .build());
        }

        MultiSearch multiSearch = new MultiSearch.Builder(searches)
                .build();

        try {
            String json = client.execute(multiSearch).getJsonString();
            JsonNode rootResponse = JsonUtils.readTree(json);
            parseResponse(rootResponse);

            List<JsonNode> responses = new ArrayList<>();
            for (JsonNode node : rootResponse.path("responses")) {
                parseResponse(node);
                responses.add(node);
            }
            return result(200, responses);
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }

    /**
     * Body:
     * <pre>
     *      {'for search endpoint'}
     * </pre>
     *
     * @param call json call
     * @return {'data': 1}
     */
    @SuppressWarnings("Duplicates")
    private Long count(JsonCall call) {
        String body = JsonUtils.toString(call.bodyAsJson());
        Count count = new Count.Builder()
                .addIndex(ElasticMapping.INDEX_NAME)
                .query(body)
                .build();

        try {
            Double number = client.execute(count).getCount();
            if (number == null) return 0L;
            return number.longValue();
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }

    /**
     * @param response response
     * @throws ElasticException if error exists
     */
    public static void parseResponse(JsonNode response) throws ElasticException {
        if (!response.has("status")) return;
        int status = response.path("status").asInt();
        if (status == 200) return;

        parseError(status, response);
        parseError(status, response.path("error").path("root_cause").path(0));
        parseError(status, response.path("error"));
        throw new ElasticException(status, JsonUtils.toString(response));
    }

    private static void parseError(int status, JsonNode error) {
        String type = error.path("type").asText();
        if (StringUtils.isNotBlank(type)) {
            String reason = error.path("reason").asText();
            throw new ElasticException(status, type + ": " + reason);
        }
    }
}
