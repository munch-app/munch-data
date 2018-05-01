package munch.data.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.searchbox.core.Search;
import munch.data.elastic.ElasticClient;
import munch.data.elastic.ElasticMarshaller;
import munch.data.structure.SearchResult;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

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
    @Deprecated
    public List<SearchResult> suggest(String text, @Nullable String latLng, int size) {
        JsonNode results = client.suggest(List.of(), text, latLng, size);
        return marshaller.deserializeList(results);
    }

    /**
     * Suggest all types given based on name
     *
     * @param types types to filter to
     * @param size  size per list
     * @param text  text query
     * @return List of SearchResult
     */
    @Deprecated
    public List<SearchResult> suggest(List<String> types, String text, @Nullable String latLng, int size) {
        JsonNode results = client.suggest(types, text, latLng, size);
        return marshaller.deserializeList(results);
    }

    /**
     * @param text   for suggesting
     * @param latLng for geo fencing to certain radius
     * @param size   for number for suggestion
     * @return List of test suggestion
     */
    public List<String> suggestText(String text, @Nullable String latLng, int size) {
        JsonNode results = client.suggest(List.of(), text, latLng, size);
        return marshaller.deserializeListName(results);
    }

    /**
     * Search all data based on name
     *
     * @param size size per list
     * @param text text query
     * @return List of SearchResult
     */
    public <T extends SearchResult> List<T> search(String text, int size) {
        JsonNode results = client.search(List.of(), text, null, 0, size);
        return marshaller.deserializeList(results);
    }

    /**
     * Search all types given based on name
     *
     * @param types  types to filter to
     * @param text   text query
     * @param latLng current user position
     * @param from   start from
     * @param size   size per list
     * @return List of SearchResult
     */
    public <T extends SearchResult> List<T> search(List<String> types, String text, @Nullable String latLng, int from, int size) {
        JsonNode results = client.search(types, text, latLng, from, size);
        return marshaller.deserializeList(results);
    }

    public <T extends SearchResult> List<T> search(Search search) {
        JsonNode results = client.search(search);
        return marshaller.deserializeList(results);
    }

    /**
     * @param map    (types, integer)
     * @param text   query for searching
     * @param latLng current user position
     * @return (types, List of SearchResult)
     */
    public Map<String, List<SearchResult>> multiSearch(Map<String, Integer> map, String text, @Nullable String latLng) {
        List<String> types = new ArrayList<>();
        List<Search> searches = new ArrayList<>();

        map.forEach((type, size) -> {
            types.add(type);
            List<String> typeList = Arrays.stream(type.split(","))
                    .map(StringUtils::trimToNull)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            searches.add(ElasticClient.createSearch(typeList, List.of("name^2", "allNames"), text, latLng, 0, size));
        });

        Map<String, List<SearchResult>> resultMap = new HashMap<>();

        List<JsonNode> results = client.postMultiSearch(searches);
        for (int i = 0; i < results.size(); i++) {
            String type = types.get(i);
            JsonNode result = results.get(i);
            resultMap.put(type, marshaller.deserializeList(result));
        }

        return resultMap;
    }
}
