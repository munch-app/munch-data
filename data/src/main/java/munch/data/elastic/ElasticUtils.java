package munch.data.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import munch.data.ElasticObject;
import munch.data.location.Cluster;
import munch.data.location.Landmark;
import munch.data.place.Place;
import munch.data.tag.Tag;
import munch.restful.core.JsonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 3/6/18
 * Time: 3:46 PM
 * Project: munch-data
 */
public final class ElasticUtils {


    /**
     * @param results from es
     * @param <T>     deserialized type
     * @return deserialized type into a list
     */
    public static <T extends ElasticObject> List<T> deserializeList(JsonNode results) {
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
    public static  <T extends ElasticObject> T deserialize(JsonNode node) {
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
