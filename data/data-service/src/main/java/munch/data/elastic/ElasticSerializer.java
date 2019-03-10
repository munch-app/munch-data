package munch.data.elastic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Singleton;
import munch.data.elastic.plugins.ElasticPlugin;
import munch.restful.core.JsonUtils;

import javax.inject.Inject;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 9/7/2017
 * Time: 1:00 AM
 * Project: munch-core
 */
@Singleton
public final class ElasticSerializer {

    private final Set<ElasticPlugin> plugins;

    @Inject
    public ElasticSerializer(Set<ElasticPlugin> plugins) {
        this.plugins = plugins;
    }

    public ObjectNode serialize(ElasticObject object) {
        ObjectNode node = JsonUtils.toTree(object);

        for (ElasticPlugin plugin : plugins) {
            plugin.serialize(object, node);
        }

        return node;
    }
}
