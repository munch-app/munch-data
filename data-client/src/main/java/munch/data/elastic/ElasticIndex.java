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
import munch.data.structure.Location;
import munch.data.structure.Place;
import munch.data.structure.Tag;

import java.io.IOException;

/**
 * Created By: Fuxing Loh
 * Date: 9/3/2017
 * Time: 3:27 PM
 * Project: munch-core
 */
@Singleton
public final class ElasticIndex {
    private final JestClient client;
    private final ObjectMapper mapper;
    private final ElasticMarshaller marshaller;

    /**
     * https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/java-docs-index.html
     *
     * @param client     injected rest client
     * @param mapper     jackson json mapper
     * @param marshaller to marshal json
     * @throws RuntimeException if ElasticSearchMapping validation failed
     */
    @Inject
    public ElasticIndex(JestClient client, ObjectMapper mapper, ElasticMarshaller marshaller) {
        this.client = client;
        this.mapper = mapper;
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

            client.execute(new Index.Builder(json)
                    .index("munch")
                    .type("Place")
                    .id(place.getId())
                    .build());
        } catch (IOException e) {
            throw new ElasticException(e);
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

            client.execute(new Index.Builder(json)
                    .index("munch")
                    .type("Location")
                    .id(location.getId())
                    .build());
        } catch (IOException e) {
            throw new ElasticException(e);
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

            client.execute(new Index.Builder(json)
                    .index("munch")
                    .type("Tag")
                    .id(tag.getId())
                    .build());
        } catch (IOException e) {
            throw new ElasticException(e);
        }
    }

    public <T> T get(String type, String key) {
        try {
            DocumentResult result = client.execute(new Get.Builder("munch", key)
                    .type(type).build());
            return marshaller.deserialize(mapper.readTree(result.getJsonString()));
        } catch (IOException e) {
            throw new ElasticException(e);
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
            throw new ElasticException(e);
        }
    }
}
