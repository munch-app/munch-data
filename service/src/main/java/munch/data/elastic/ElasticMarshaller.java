package munch.data.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Singleton;
import munch.data.ElasticObject;
import munch.data.SuggestObject;
import munch.data.location.Cluster;
import munch.data.location.Landmark;
import munch.data.place.Place;
import munch.data.tag.Tag;
import munch.restful.core.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 9/7/2017
 * Time: 1:00 AM
 * Project: munch-core
 */
@Singleton
public final class ElasticMarshaller {
    private static final Logger logger = LoggerFactory.getLogger(ElasticMarshaller.class);
    private static final ObjectMapper mapper = JsonUtils.objectMapper;

    public ObjectNode serialize(ElasticObject object) {
        ObjectNode node = mapper.valueToTree(object);

        // SuggestObject Field
        if (object instanceof SuggestObject) {
            node.putObject("suggest")
                    .put("weight", 1)
                    .putArray("input")
                    .add(((SuggestObject) object).getName());
        }

        return node;
    }

    /**
     * @param results from es
     * @param <T>     deserialized type
     * @return deserialized type into a list
     */
    public <T extends ElasticObject> List<T> deserializeList(JsonNode results) {
        if (results.isMissingNode()) return Collections.emptyList();

        List<T> list = new ArrayList<>();
        for (JsonNode result : results) list.add(deserialize(result));
        return list;
    }

    /**
     * @param node node to deserialize
     * @param <T>  deserialized type
     * @return deserialized type
     */
    @SuppressWarnings("unchecked")
    public <T extends ElasticObject> T deserialize(JsonNode node) {
        JsonNode source = node.path("_source");
        switch (source.path("dataType").asText()) {
            case "Tag":
                return (T) JsonUtils.toObject(source, Tag.class);
            case "Landmark":
                return (T) JsonUtils.toObject(source, Landmark.class);
            case "Cluster":
                return (T) JsonUtils.toObject(source, Cluster.class);
            case "Place":
                return (T) JsonUtils.toObject(source, Place.class);
            default:
                return null;
        }
    }
}
