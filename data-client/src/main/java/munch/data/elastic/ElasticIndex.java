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
        try {
            ObjectNode node = marshaller.serialize(place);
            String json = mapper.writeValueAsString(node);

            DocumentResult result = client.execute(new Index.Builder(json)
                    .index("munch")
                    .type("Place")
                    .id(place.getId())
                    .build());

            parseResult(result);
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }

    /**
     * Index a location by putting it into elastic search
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/geo-shape.html
     *
     * @param location location to index
     * @throws ElasticException wrapped exception
     */
    public void put(Location location) throws ElasticException {
        try {
            ObjectNode node = marshaller.serialize(location);
            String json = mapper.writeValueAsString(node);

            DocumentResult result = client.execute(new Index.Builder(json)
                    .index("munch")
                    .type("Location")
                    .id(location.getId())
                    .build());

            parseResult(result);
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }

    /**
     * Index a tag by putting it into elastic search
     *
     * @param tag tag to index
     * @throws ElasticException wrapped exception
     */
    public void put(Tag tag) throws ElasticException {
        try {
            ObjectNode node = marshaller.serialize(tag);
            String json = mapper.writeValueAsString(node);

            DocumentResult result = client.execute(new Index.Builder(json)
                    .index("munch")
                    .type("Tag")
                    .id(tag.getId())
                    .build());

            parseResult(result);
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }

    /**
     * Index a Container by putting it into elastic search
     *
     * @param container container to index
     * @throws ElasticException wrapped exception
     */
    public void put(Container container) throws ElasticException {
        try {
            ObjectNode node = marshaller.serialize(container);
            String json = mapper.writeValueAsString(node);

            DocumentResult result = client.execute(new Index.Builder(json)
                    .index("munch")
                    .type("Container")
                    .id(container.getId())
                    .build());

            parseResult(result);
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }

    private static void parseResult(DocumentResult result) {
        if (result.getType() == null) {
            logger.warn("{}", result.getJsonString());
        } else {
            throw new ElasticException("Failed to put object.");
        }
    }

    public <T> T get(String type, String key) {
        try {
            DocumentResult result = client.execute(new Get.Builder("munch", key)
                    .type(type).build());
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
            client.execute(new Delete.Builder(key)
                    .index("munch")
                    .type(type)
                    .build());
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }
}
