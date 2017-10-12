package munch.data.clients;

import com.amazonaws.services.dynamodbv2.document.*;
import com.fasterxml.jackson.databind.JsonNode;
import munch.data.structure.PlaceDynamicCard;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Dynamic Place Card Client
 * <p>
 * Created by: Fuxing
 * Date: 12/10/2017
 * Time: 8:02 AM
 * Project: munch-data
 */
public class PlaceCardClient extends AbstractClient {
    public static final String DYNAMO_TABLE_NAME = "munch-data.PlaceCard";

    private final Table cardTable;

    @Inject
    public PlaceCardClient(DynamoDB dynamoDB) {
        this.cardTable = dynamoDB.getTable(DYNAMO_TABLE_NAME);
    }

    /**
     * @param placeId placeId
     * @return List of all the dynamic cards in table
     */
    public List<PlaceDynamicCard> list(String placeId) {
        Objects.requireNonNull(placeId);

        ItemCollection<QueryOutcome> collection = cardTable.query("_placeId", placeId);

        // Collect results
        List<PlaceDynamicCard> cards = new ArrayList<>();
        for (Item item : collection) {
            String cardName = item.getString("_cardName");
            String cardVersion = item.getString("_cardVersion");
            JsonNode data = fromJson(item.getJSON("_data"), JsonNode.class);
            cards.add(new DefaultCard(cardName, cardVersion, data));
        }
        return cards;
    }

    /**
     * @param placeId  placeId
     * @param cardName cardName
     * @return get DynamicCard with placeId and cardName
     */
    @Nullable
    public PlaceDynamicCard get(String placeId, String cardName) {
        Objects.requireNonNull(placeId);
        Objects.requireNonNull(cardName);

        Item item = cardTable.getItem("_placeId", placeId,
                "_cardName", cardName);
        if (item == null) return null;

        String cardVersion = item.getString("_cardVersion");
        JsonNode data = fromJson(item.getJSON("_data"), JsonNode.class);
        return new DefaultCard(cardName, cardVersion, data);
    }

    /**
     * @param placeId place id where the card belongs to
     * @param card    card to put
     */
    public void put(String placeId, PlaceDynamicCard card) {
        Objects.requireNonNull(placeId);
        Objects.requireNonNull(card.getCardName());
        Objects.requireNonNull(card.getCardVersion());
        Objects.requireNonNull(card.getData());

        cardTable.putItem(new Item()
                .with("_placeId", placeId)
                .with("_cardName", card.getCardName())
                .with("_cardVersion", card.getCardVersion())
                .withJSON("_data", toJson(card.getData())));
    }

    /**
     * @param placeId  place id where the card belongs to
     * @param cardName name of the card
     */
    public void delete(String placeId, String cardName) {
        Objects.requireNonNull(placeId);
        Objects.requireNonNull(cardName);

        cardTable.deleteItem("_placeId", placeId,
                "_cardName", cardName);
    }

    /**
     * DefaultCard for static creation of cardName, cardVersion and data
     */
    private static class DefaultCard extends PlaceDynamicCard {
        private final String cardName;
        private final String cardVersion;
        private final JsonNode data;

        private DefaultCard(String cardName, String cardVersion, JsonNode data) {
            this.cardName = cardName;
            this.cardVersion = cardVersion;
            this.data = data;
        }

        @Override
        public String getCardName() {
            return cardName;
        }

        @Override
        public String getCardVersion() {
            return cardVersion;
        }

        @Override
        public JsonNode getData() {
            return data;
        }
    }
}
