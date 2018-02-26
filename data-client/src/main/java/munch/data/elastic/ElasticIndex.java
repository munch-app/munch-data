package munch.data.elastic;

import catalyst.utils.iterators.NestedIterator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import io.searchbox.params.Parameters;
import munch.data.exceptions.ClusterBlockException;
import munch.data.exceptions.ElasticException;
import munch.data.structure.Container;
import munch.data.structure.Location;
import munch.data.structure.Place;
import munch.data.structure.Tag;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created By: Fuxing Loh
 * Date: 9/3/2017
 * Time: 3:27 PM
 * Project: munch-core
 */
@Singleton
public final class ElasticIndex {
    private static final Logger logger = LoggerFactory.getLogger(ElasticIndex.class);
    private static final ObjectMapper mapper = JsonUtils.objectMapper;

    private final JestClient client;
    private final ElasticMarshaller marshaller;

    /**
     * https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/java-docs-index.html
     *
     * @param client     injected rest client
     * @param marshaller to marshal json
     * @throws RuntimeException if ElasticSearchMapping validation failed
     */
    @Inject
    public ElasticIndex(JestClient client, ElasticMarshaller marshaller) {
        this.client = client;
        this.marshaller = marshaller;
    }

    /**
     * Index a place by putting it into elastic search
     *
     * @param place place to index
     * @throws ElasticException wrapped exception
     */
    public void put(Place place) throws ElasticException {
        ObjectNode node = marshaller.serialize(place);
        put("Place", place.getId(), node);
    }

    /**
     * Index a location by putting it into elastic search
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/geo-shape.html
     *
     * @param location location to index
     * @throws ElasticException wrapped exception
     */
    public void put(Location location) throws ElasticException {
        ObjectNode node = marshaller.serialize(location);
        put("Location", location.getId(), node);
    }

    /**
     * Index a tag by putting it into elastic search
     *
     * @param tag tag to index
     * @throws ElasticException wrapped exception
     */
    public void put(Tag tag) throws ElasticException {
        ObjectNode node = marshaller.serialize(tag);
        put("Tag", tag.getId(), node);
    }

    /**
     * Index a Container by putting it into elastic search
     *
     * @param container container to index
     * @throws ElasticException wrapped exception
     */
    public void put(Container container) throws ElasticException {
        ObjectNode node = marshaller.serialize(container);
        put("Container", container.getId(), node);
    }

    private void put(String type, String key, ObjectNode node) throws ElasticException {
        try {
            String json = mapper.writeValueAsString(node);
            DocumentResult result = client.execute(new Index.Builder(json)
                    .index(ElasticMapping.INDEX_NAME)
                    .type("Data")
                    .id(createKey(type, key))
                    .build());

            validateResult(true, result);
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }

    public <T> T get(String type, String key) {
        try {
            DocumentResult result = client.execute(new Get.Builder(ElasticMapping.INDEX_NAME, createKey(type, key)).type("Data").build());
            validateResult(false, result);
            return marshaller.deserialize(mapper.readTree(result.getJsonString()));
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }

    /**
     * @param type    data type to scroll
     * @param timeout timeout in elastic time unit e.g. 1m
     * @param size    size per batch
     * @param <T>     data type
     * @return Iterator of batch Data
     */
    public <T> Iterator<List<T>> scroll(String type, String timeout, int size) {
        Search search = new Search.Builder("{\"query\":{\"term\":{\"dataType\":\"" + type + "\"}}, \"size\": " + size + "}")
                .addIndex(ElasticMapping.INDEX_NAME)
                .setParameter(Parameters.SCROLL, timeout)
                .build();

        try {
            return new Iterator<>() {
                JsonNode result = mapper.readTree(client.execute(search).getJsonString());
                List<T> nextList = marshaller.deserializeList(result.path("hits").path("hits"));

                @Override
                public boolean hasNext() {
                    if (nextList == null) {
                        try {
                            SearchScroll scroll = new SearchScroll.Builder(result.path("_scroll_id").asText(), timeout).build();
                            result = mapper.readTree(client.execute(scroll).getJsonString());
                            nextList = marshaller.deserializeList(result.path("hits").path("hits"));

                            if (!nextList.isEmpty()) return true;

                            ClearScroll clearScroll = new ClearScroll.Builder().addScrollId(result.path("_scroll_id").asText()).build();
                            client.execute(clearScroll);
                            return false;
                        } catch (IOException e) {
                            throw ElasticException.parse(e);
                        }
                    }

                    return !nextList.isEmpty();
                }

                @Override
                public List<T> next() {
                    List<T> temp = nextList;
                    nextList = null;
                    return temp;
                }
            };

        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }

    /**
     * @param type    type: Place, Container, Location, Tag
     * @param timeout timeout different each batched request
     * @param <T>     T data type
     * @return Iterator of that Data
     */
    public <T> Iterator<T> scroll(String type, String timeout) {
        return new NestedIterator<T, List<T>>(scroll(type, timeout, 20), List::iterator);
    }

    /**
     * @param type data type to delete before
     * @param key  key of data type
     * @throws ElasticException wrapped exception
     */
    public void delete(String type, String key) {
        try {
            DocumentResult result = client.execute(new Delete.Builder(createKey(type, key))
                    .index(ElasticMapping.INDEX_NAME)
                    .type("Data")
                    .build());

            validateResult(false, result);
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }

    private void validateResult(boolean validateNotFound, DocumentResult result) {
        if (result.getErrorMessage() != null) {
            JsonNode jsonNode = JsonUtils.readTree(result.getJsonString());
            String errorType = jsonNode.path("error").path("type").asText();
            if (StringUtils.equals(errorType, "cluster_block_exception")) {
                throw new ClusterBlockException();
            }

            if (StringUtils.equals(jsonNode.path("result").asText(), "not_found")) {
                if (validateNotFound) return;
                throw new ElasticException(404, "Failed to put/delete/get object.");
            }

            logger.warn("{}", jsonNode);
            throw new ElasticException("Failed to put/delete/get object.");
        }
    }

    private String createKey(String type, String key) {
        return type + "|" + key;
    }
}
