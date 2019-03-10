package munch.data.resolver;

import com.fasterxml.jackson.databind.node.ObjectNode;
import munch.data.client.ElasticClient;
import munch.data.elastic.ElasticUtils;
import munch.data.location.Landmark;
import munch.restful.core.JsonUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 7/6/18
 * Time: 11:48 AM
 * Project: munch-data
 */
@Singleton
public final class LandmarkResolver {

    private final ElasticClient elasticClient;

    @Inject
    public LandmarkResolver(ElasticClient elasticClient) {
        this.elasticClient = elasticClient;
    }

    public List<Landmark> resolve(String latLng) {
        ObjectNode root = JsonUtils.createObjectNode();
        root.put("from", 0);
        root.put("size", 1);

        ObjectNode bool = JsonUtils.createObjectNode();
        bool.set("must", ElasticUtils.mustMatchAll());
        bool.set("filter", JsonUtils.createArrayNode()
                .add(ElasticUtils.filterTerm("dataType", "Landmark"))
        );
        root.putObject("query").set("bool", bool);

        root.set("sort", JsonUtils.createArrayNode()
                .add(ElasticUtils.Sort.sortDistance(latLng))
        );

        return elasticClient.searchHitsHits(root);
    }
}
