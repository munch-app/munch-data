package munch.data.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Singleton;
import munch.data.tag.Tag;
import munch.restful.core.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 9/7/2017
 * Time: 1:00 AM
 * Project: munch-core
 */
@Singleton
@SuppressWarnings("Duplicates") // TODO Remove after done
public final class ElasticMarshaller {
    private static final Logger logger = LoggerFactory.getLogger(ElasticMarshaller.class);
    private static final ObjectMapper mapper = JsonUtils.objectMapper;

    public ObjectNode serialize(Tag tag) {
        ObjectNode node = mapper.createObjectNode();
        node.put("dataType", tag.getDataType());

        // Root Node
        node.put("id", tag.getTagId());
        node.put("type", tag.getType());
        node.put("name", tag.getName());
        node.put("names", JsonUtils.toTree(tag.getNames()));

        // Suggest Field
        ArrayNode inputs = mapper.createArrayNode();
        inputs.add(tag.getName());
        node.putObject("suggest")
                .put("weight", 100)
                .set("input", inputs);
        return node;
    }

    /**
     * @param node json node
     * @return deserialized Tag
     */
    public Tag deserializeTag(JsonNode node) {
        Tag tag = new Tag();
        tag.setTagId(node.get("id").asText());
        tag.setType(node.get("type").asText());
        tag.setName(node.get("name").asText());

        tag.setNames(new HashSet<>());
        node.get("names").forEach(n -> tag.getNames().add(n.asText()));

        return tag;
    }

    /**
     * @param results from es
     * @param <T>     deserialized type
     * @return deserialized type into a list
     */
    public <T> List<T> deserializeList(JsonNode results) {
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
    public <T> T deserialize(JsonNode node) {
        JsonNode source = node.path("_source");
        switch (source.path("dataType").asText()) {
            case "Tag":
                return (T) deserializeTag(source);
            default:
                return null;
        }
    }
}
