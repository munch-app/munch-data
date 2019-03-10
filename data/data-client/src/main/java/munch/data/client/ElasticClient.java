package munch.data.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import munch.data.elastic.ElasticObject;
import munch.data.elastic.DataType;
import munch.data.elastic.ElasticUtils;
import munch.data.location.City;
import munch.data.location.Country;
import munch.data.place.Place;
import munch.restful.client.RestfulClient;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 2/6/18
 * Time: 6:13 PM
 * Project: munch-data
 */
@Singleton
public final class ElasticClient extends RestfulClient {

    @Inject
    public ElasticClient() {
        this(ConfigFactory.load().getString("services.munch-data.url"));
    }

    ElasticClient(String url) {
        super(url);
    }

    /**
     * @param node search node
     * @return search result
     */
    public JsonNode search(JsonNode node) {
        return doPost("/elastic/search")
                .body(node)
                .asDataNode();
    }

    /**
     * @param node search node
     * @param <T>  Data Type
     * @return List of ElasticObject
     */
    public <T extends ElasticObject> List<T> searchHitsHits(JsonNode node) {
        return ElasticUtils.deserializeList(search(node).path("hits").path("hits"));
    }

    /**
     * @param nodes list of search node
     * @return list of search result
     */
    public List<JsonNode> searchMulti(List<JsonNode> nodes) {
        return doPost("/elastic/search/multi")
                .body(nodes)
                .asDataList(JsonNode.class);
    }

    /**
     * @param node search node
     * @return count or null
     */
    @Nullable
    public Long count(JsonNode node) {
        return doPost("/elastic/count")
                .body(node)
                .asDataObject(Long.class);
    }

    /**
     * This suggest will return results from
     * - DataType.Brand
     * - DataType.Place
     * - DataType.Area
     * - DataType.Tag
     * <p>
     * if LatLng is provided:
     * Data inside the provided latLng will be boosted by 3 with precision 6 (1.2km x 609.4m)
     *
     * @param country to search, within
     * @param text    to search
     * @param size    of ElasticObject to return
     * @return List of ElasticObject, contains all 4 types stated above
     */
    public List<ElasticObject> suggest(Country country, String text, @Nullable String latLng, int size) {
        if (StringUtils.isBlank(text)) return List.of();

        ObjectNode contexts = JsonUtils.createObjectNode();
        contexts.set("dataType", ElasticUtils.Suggest.makeDataType(DataType.Brand, DataType.Place, DataType.Area, DataType.Tag));

        // Boost inside, (1.2km x 609.4m)
        if (latLng != null) {
            contexts.set("latLng", ElasticUtils.Suggest.makeLatLng(latLng, 6, 3));
        }

        JsonNode completion = ElasticUtils.Suggest.makeCompletion(country.getSuggestField(), contexts, size);
        return suggestPrefix(text, completion);
    }

    /**
     * @param city optional city to search within
     * @param text text for searching, non-blank, else returns empty list
     * @param size of Place to return
     * @return List of Place that fit the prefix
     */
    public List<Place> suggestPlaces(@Nullable City city, String text, int size) {
        if (StringUtils.isBlank(text)) return List.of();

        if (city == null) {
            return suggestPrefix(text, ElasticUtils.Suggest.makeCompletion("suggest_places", null, size));
        }

        ObjectNode contexts = JsonUtils.createObjectNode();
        contexts.set("city", ElasticUtils.Suggest.makeCity(city));
        return suggestPrefix(text, ElasticUtils.Suggest.makeCompletion("suggest_places", contexts, size));
    }

    public <T extends ElasticObject> List<T> suggestPrefix(String text, JsonNode completion) {
        ObjectNode root = JsonUtils.createObjectNode((object) -> {
            object.putObject("suggest")
                    .putObject("suggestions")
                    .put("prefix", StringUtils.lowerCase(text))
                    .set("completion", completion);
        });

        JsonNode results = search(root).path("suggest")
                .path("suggestions")
                .path(0)
                .path("options");
        if (results.isMissingNode()) return List.of();

        return ElasticUtils.deserializeList(results);
    }
}
