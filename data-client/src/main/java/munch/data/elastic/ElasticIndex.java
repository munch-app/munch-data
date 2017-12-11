package munch.data.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.searchbox.client.JestClient;
import io.searchbox.core.Delete;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Get;
import io.searchbox.core.Index;
import munch.data.exceptions.ElasticException;
import munch.data.structure.Container;
import munch.data.structure.Location;
import munch.data.structure.Place;
import munch.data.structure.Tag;
import munch.restful.core.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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

    private void put(String type, String key, ObjectNode node) {
        try {
            String json = mapper.writeValueAsString(node);
            DocumentResult result = client.execute(new Index.Builder(json)
                    .index("munch")
                    .type("Data")
                    .id(createKey(type, key))
                    .build());

            if (result.getErrorMessage() != null) {
                logger.warn("{}", result.getJsonString());
                throw new ElasticException("Failed to put object.");
            }
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }

    public <T> T get(String type, String key) {
        try {
            DocumentResult result = client.execute(new Get.Builder("munch", createKey(type, key)).type("Data").build());
            return marshaller.deserialize(mapper.readTree(result.getJsonString()));
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }

    /**
     * @param type data type to delete before
     * @param key  key of data type
     * @throws ElasticException wrapped exception
     */
    public void delete(String type, String key) {
        try {
            client.execute(new Delete.Builder(createKey(type, key))
                    .index("munch")
                    .type("Data")
                    .build());
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }

    private String createKey(String type, String key) {
        return type + "|" + key;
    }
}
