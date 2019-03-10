package munch.data.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import munch.data.elastic.ElasticObject;
import munch.data.elastic.DataType;
import munch.data.elastic.ElasticUtils;
import munch.restful.client.RestfulClient;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.StringUtils;

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
     * @param node search node
     * @param <T>  Data Type
     * @return List of ElasticObject
     */
    public <T extends ElasticObject> List<T> searchHitsHits(JsonNode node) {
        return ElasticUtils.deserializeList(search(node).path("hits").path("hits"));
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

    /**
     * @param dataType to filter to
     * @param text     to search
     * @param size     of ElasticObject to return
     * @param <T>      ObjectType
     * @return List of ElasticObject
     */
    public <T extends ElasticObject> List<T> suggest(DataType dataType, String text, @Nullable String latLng, int size) {
        if (StringUtils.isBlank(text)) return List.of();

        ObjectNode root = JsonUtils.createObjectNode((object) -> {
            object.putObject("suggest")
                    .putObject("suggestions")
                    .put("prefix", StringUtils.lowerCase(text))
                    .set("completion", ElasticUtils.Suggest.makeCompletion(dataType, latLng, size));
        });

        JsonNode results = search(root).path("suggest")
                .path("suggestions")
                .path(0)
                .path("options");
        if (results.isMissingNode()) return List.of();

        return ElasticUtils.deserializeList(results);
    }
}
