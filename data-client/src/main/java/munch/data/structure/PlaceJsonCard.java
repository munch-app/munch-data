package munch.data.structure;

import com.fasterxml.jackson.databind.JsonNode;
import munch.restful.core.JsonUtils;

/**
 * Created By: Fuxing Loh
 * Date: 12/10/2017
 * Time: 9:31 AM
 * Project: munch-data
 */
public class PlaceJsonCard implements PlaceCard<JsonNode> {
    private final String cardId;
    private final JsonNode data;

    /**
     * @param cardId static card id
     * @param data DynamicCard data type is bound to JsonNode
     */
    public PlaceJsonCard(String cardId, JsonNode data) {
        this.cardId = cardId;
        this.data = data;
    }

    /**
     * @param cardId static card id
     * @param object DynamicCard data type is bound to JsonNode
     */
    public PlaceJsonCard(String cardId, Object object) {
        this(cardId, JsonUtils.toTree(object));
    }

    @Override
    public String getCardId() {
        return cardId;
    }

    @Override
    public JsonNode getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlaceJsonCard that = (PlaceJsonCard) o;

        if (!cardId.equals(that.cardId)) return false;
        return data.equals(that.data);
    }

    @Override
    public int hashCode() {
        int result = cardId.hashCode();
        result = 31 * result + data.hashCode();
        return result;
    }
}
