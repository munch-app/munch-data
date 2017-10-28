package munch.data.structure;

import com.fasterxml.jackson.databind.JsonNode;
import munch.restful.core.JsonUtils;

/**
 * Created By: Fuxing Loh
 * Date: 12/10/2017
 * Time: 9:31 AM
 * Project: munch-data
 */
public abstract class PlaceJsonCard implements PlaceCard<JsonNode> {
    private final JsonNode data;

    /**
     * @param data DynamicCard data type is bound to JsonNode
     */
    protected PlaceJsonCard(JsonNode data) {
        this.data = data;
    }

    /**
     * @param object DynamicCard data type is bound to JsonNode
     */
    protected PlaceJsonCard(Object object) {
        this(JsonUtils.toTree(object));
    }

    @Override
    public JsonNode getData() {
        return data;
    }
}
