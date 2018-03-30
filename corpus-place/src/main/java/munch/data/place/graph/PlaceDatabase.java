package munch.data.place.graph;

import catalyst.utils.exception.ExceptionRetriable;
import catalyst.utils.exception.Retriable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import corpus.data.CorpusClient;
import corpus.data.CorpusData;
import corpus.data.DocumentClient;
import corpus.field.PlaceKey;
import munch.data.clients.PlaceClient;
import munch.data.place.PlaceParser;
import munch.data.structure.Place;
import munch.restful.core.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * In Charge of saving Place Data:
 * - to Document   -> PlaceTree
 * - to MunchData  -> Elasticsearch
 * - to MunchData  -> DynamoDB
 * - to Corpus     -> Sg.Munch.Place
 * <p>
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 8:06 PM
 * Project: munch-data
 */
@Singleton
public class PlaceDatabase {
    private static final Logger logger = LoggerFactory.getLogger(PlaceDatabase.class);
    private static final String TABLE_NAME = "Sg.Munch.Place.Tree.V2";

    private static final Retriable retriable = new ExceptionRetriable(4);

    private final DocumentClient documentClient;
    private final CorpusClient corpusClient;
    private final PlaceClient placeClient;

    private final PlaceParser placeParser;

    @Inject
    public PlaceDatabase(DocumentClient documentClient, CorpusClient corpusClient, PlaceClient placeClient, PlaceParser placeParser) {
        this.documentClient = documentClient;
        this.corpusClient = corpusClient;
        this.placeClient = placeClient;
        this.placeParser = placeParser;
    }

    /**
     * @param placeId id of place, aka catalyst id
     * @return PlaceTree, nullable
     */
    @Nullable
    public PlaceTree get(String placeId) {
        JsonNode node = documentClient.get(TABLE_NAME, placeId, "0");
        if (node == null) return null;

        return JsonUtils.toObject(node.get("tree"), PlaceTree.class);
    }

    /**
     * @param placeId   id of place
     * @param placeTree tree data
     * @param decayed   whether data has successfully decayed
     */
    public void put(String placeId, PlaceTree placeTree, boolean decayed) {
        ObjectNode node = JsonUtils.objectMapper.createObjectNode();
        node.set("tree", JsonUtils.toTree(placeTree));
        documentClient.put(TABLE_NAME, placeId, "0", node);

        Place place = placeParser.parse(placeId, placeTree, decayed);

        if (place == null) {
            logger.error("Failed to parse Place: {}", placeId);
            return;
        }


        putIf(place);
        corpusClient.put("Sg.Munch.Place", placeId, createCorpusData(place));
    }

    /**
     * Data put only if actually changed
     *
     * @param place non null place
     */
    private void putIf(Place place) {
        Place existing = placeClient.get(place.getId());

        // Put if data is changed only
        if (!place.equals(existing)) {
            try {
                retriable.loop(() -> placeClient.put(place));
                // Data might have been added to deleted, remove from deleted list

                logger.info("Updated: updated: {} existing: {}",
                        JsonUtils.toString(place),
                        JsonUtils.toString(existing)
                );
            } catch (Exception e) {
                logger.error("Error: updated: {}", JsonUtils.toString(place));
                throw e;
            }
        }
    }

    public void delete(String placeId) {
        placeClient.delete(placeId);
        corpusClient.delete("Sg.Munch.Place", placeId);
        documentClient.delete(TABLE_NAME, placeId, "0");
    }

    public static CorpusData createCorpusData(Place place) {
        // Put to corpus client
        CorpusData placeData = new CorpusData(System.currentTimeMillis());
        // Max name is put twice
        placeData.put(PlaceKey.name, place.getName());
        if (place.getAllNames() != null)
            place.getAllNames().forEach(name -> placeData.put(PlaceKey.name, name));

        placeData.put(PlaceKey.phone, place.getPhone());
        placeData.put(PlaceKey.website, place.getWebsite());
        placeData.put(PlaceKey.description, place.getDescription());

        placeData.put(PlaceKey.Location.street, place.getLocation().getStreet());
        placeData.put(PlaceKey.Location.address, place.getLocation().getAddress());
        placeData.put(PlaceKey.Location.unitNumber, place.getLocation().getUnitNumber());

        placeData.put(PlaceKey.Location.city, place.getLocation().getCity());
        placeData.put(PlaceKey.Location.country, place.getLocation().getCountry());

        placeData.put(PlaceKey.Location.postal, place.getLocation().getPostal());
        placeData.put(PlaceKey.Location.latLng, place.getLocation().getLatLng());
        return placeData;
    }
}
