package munch.data.clients;

import com.amazonaws.services.dynamodbv2.document.*;
import com.fasterxml.jackson.databind.JsonNode;
import munch.data.structure.PlaceJsonCard;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

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
    private static final String _placeId = "_placeId";
    private static final String _cardId = "_cardId";
    private static final String _data = "_data";

    private final Table cardTable;

    @Inject
    public PlaceCardClient(DynamoDB dynamoDB) {
        this.cardTable = dynamoDB.getTable(DYNAMO_TABLE_NAME);
    }

    /**
     * @param placeId placeId
     * @return List of all the dynamic cards in table
     */
    public List<PlaceJsonCard> list(String placeId) {
        Objects.requireNonNull(placeId);

        ItemCollection<QueryOutcome> collection = cardTable.query(_placeId, placeId);

        // Collect results
        List<PlaceJsonCard> cards = new ArrayList<>();
        for (Item item : collection) {
            String cardId = item.getString(_cardId);
            JsonNode data = fromJson(item.getJSON(_data), JsonNode.class);
            cards.add(new DefaultCard(cardId, data));
        }
        return cards;
    }

    /**
     * @param placeId placeId
     * @param cardId  card id
     * @return get DynamicCard with placeId and cardName
     */
    @Nullable
    public PlaceJsonCard get(String placeId, String cardId) {
        Objects.requireNonNull(placeId);
        Objects.requireNonNull(cardId);

        Item item = cardTable.getItem(_placeId, placeId, _cardId, cardId);
        if (item == null) return null;

        JsonNode data = fromJson(item.getJSON(_data), JsonNode.class);
        return new DefaultCard(cardId, data);
    }

    /**
     * @param placeId place id where the card belongs to
     * @param card    card to put
     */
    public void put(String placeId, PlaceJsonCard card) {
        Objects.requireNonNull(placeId);
        Objects.requireNonNull(card.getCardId());
        Objects.requireNonNull(card.getData());

        cardTable.putItem(new Item()
                .with(_placeId, placeId)
                .with(_cardId, card.getCardId())
                .withJSON(_data, toJson(card.getData())));
    }

    /**
     * @param placeId   place id where the card belongs to
     * @param card      card to put
     * @param predicate predicate to check if to put, true = delete
     */
    public void putIf(String placeId, PlaceJsonCard card, Function<PlaceJsonCard, Boolean> predicate) {
        Objects.requireNonNull(placeId);
        Objects.requireNonNull(card.getCardId());
        Objects.requireNonNull(card.getData());
        Objects.requireNonNull(predicate);

        PlaceJsonCard existing = get(placeId, card.getCardId());
        if (predicate.apply(existing)) {
            cardTable.putItem(new Item()
                    .with(_placeId, placeId)
                    .with(_cardId, card.getCardId())
                    .withJSON(_data, toJson(card.getData())));
        }
    }

    /**
     * @param placeId place id where the card belongs to
     * @param cardId  card id
     */
    public void delete(String placeId, String cardId) {
        Objects.requireNonNull(placeId);
        Objects.requireNonNull(cardId);

        cardTable.deleteItem(_placeId, placeId, _cardId, cardId);
    }

    /**
     * @param placeId   place id where the card belongs to
     * @param cardId    card id
     * @param predicate predicate to check if to delete, true = delete
     */
    public void deleteIf(String placeId, String cardId, Function<PlaceJsonCard, Boolean> predicate) {
        Objects.requireNonNull(placeId);
        Objects.requireNonNull(cardId);
        Objects.requireNonNull(predicate);

        PlaceJsonCard existing = get(placeId, cardId);
        if (predicate.apply(existing)) {
            cardTable.deleteItem(_placeId, placeId, _cardId, cardId);
        }
    }

    /**
     * DefaultCard for static creation of cardName, cardVersion and data
     */
    private static class DefaultCard extends PlaceJsonCard {
        private final String cardId;

        private DefaultCard(String cardId, JsonNode data) {
            super(data);
            this.cardId = cardId;
        }

        @Override
        public String getCardId() {
            return cardId;
        }
    }
}
