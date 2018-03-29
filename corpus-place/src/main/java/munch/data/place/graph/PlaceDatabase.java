package munch.data.place.graph;

import com.fasterxml.jackson.databind.JsonNode;
import corpus.data.CorpusClient;
import corpus.data.DocumentClient;
import munch.data.clients.PlaceClient;
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
    private static final String TABLE_NAME = "Sg.Munch.Place.Tree";

    private final DocumentClient documentClient;
    private final CorpusClient corpusClient;
    private final PlaceClient placeClient;

    @Inject
    public PlaceDatabase(DocumentClient documentClient, CorpusClient corpusClient, PlaceClient placeClient) {
        this.documentClient = documentClient;
        this.corpusClient = corpusClient;
        this.placeClient = placeClient;
    }

    /**
     * @param placeId id of place, aka catalyst id
     * @return PlaceTree, nullable
     */
    @Nullable
    public PlaceTree get(String placeId) {
        JsonNode node = documentClient.get("Sg.Munch.Place.Tree", placeId, "0");
        if (node == null) return null;

        return JsonUtils.toObject(node.get("tree"), PlaceTree.class);
    }

    public void put(PlaceTree placeTree) {
        // TODO put document, munch & corpus
        // not to update if no change, try compare?
    }

    public void delete(String placeId) {
        placeClient.delete(placeId);
        corpusClient.delete("Sg.Munch.Place", placeId);
        documentClient.delete(TABLE_NAME, placeId, "0");
    }
}
