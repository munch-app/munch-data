package munch.data.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import munch.data.elastic.ElasticClient;
import munch.data.elastic.ElasticMarshaller;
import munch.data.structure.SearchResult;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 10/7/2017
 * Time: 6:29 PM
 * Project: munch-core
 */
@Singleton
public class SearchClient extends AbstractClient {

    private final ElasticClient client;
    private final ElasticMarshaller marshaller;

    @Inject
    public SearchClient(ElasticClient client, ElasticMarshaller marshaller) {
        this.client = client;
        this.marshaller = marshaller;
    }

    /**
     * Suggest all data based on name
     *
     * @param size size per list
     * @param text text query
     * @return List of SearchResult
     */
    public List<SearchResult> suggest(String text, @Nullable String latLng, int size) {
        JsonNode results = client.suggest(List.of(), text, latLng, size);
        return marshaller.deserializeList(results);
    }

    /**
     * Search all data based on name
     *
     * @param size size per list
     * @param text text query
     * @return List of SearchResult
     */
    public List<SearchResult> search(String text, int size) {
        JsonNode results = client.search(List.of(), text, size);
        return marshaller.deserializeList(results);
    }

    /**
     * Search all types given based on name
     *
     * @param types types to filter to
     * @param size  size per list
     * @param text  text query
     * @return List of SearchResult
     */
    public List<SearchResult> search(List<String> types, String text, int size) {
        JsonNode results = client.search(types, text, size);
        return marshaller.deserializeList(results);
    }
}
