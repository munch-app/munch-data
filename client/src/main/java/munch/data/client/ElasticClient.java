package munch.data.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.ConfigFactory;
import munch.restful.client.RestfulClient;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 2/6/18
 * Time: 6:13 PM
 * Project: munch-data
 */
@Singleton
public final class ElasticClient extends RestfulClient {

    @Inject
    public ElasticClient() {
        this(ConfigFactory.load().getString("services.munch-data.url"));
    }

    ElasticClient(String url) {
        super(url);
    }

    /**
     * @param node search node
     * @return search result
     */
    public JsonNode search(JsonNode node) {
        return doPost("/elastic/search")
                .body(node)
                .asDataNode();
    }

    /**
     * @param nodes list of search node
     * @return list of search result
     */
    public List<JsonNode> searchMulti(List<JsonNode> nodes) {
        return doPost("/elastic/search/multi")
                .body(nodes)
                .asDataList(JsonNode.class);
    }

    /**
     * @param node search node
     * @return count or null
     */
    @Nullable
    public Long count(JsonNode node) {
        return doPost("/elastic/count")
                .body(node)
                .asDataObject(Long.class);
    }
}
