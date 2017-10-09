package munch.data.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.searchbox.client.JestClient;
import io.searchbox.core.Delete;
import io.searchbox.core.Index;
import munch.data.structure.Location;
import munch.data.structure.Place;
import munch.data.structure.Tag;

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
     * @param place   place to index
     * @throws Exception any exception
     */
    public void put(Place place) throws Exception {
        ObjectNode node = marshaller.serialize(place);
        String json = mapper.writeValueAsString(node);

        client.execute(new Index.Builder(json)
                .index("munch")
                .type("place")
                .id(place.getId())
                .build());
    }

    /**
     * Index a location by putting it into elastic search
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/geo-shape.html
     *
     * @param location location to index
     * @throws Exception any exception
     */
    public void put(Location location) throws Exception {
        ObjectNode node = marshaller.serialize(location);
        String json = mapper.writeValueAsString(node);

        client.execute(new Index.Builder(json)
                .index("munch")
                .type("location")
                .id(location.getId())
                .build());
    }

    /**
     * Index a tag by putting it into elastic search
     *
     * @param tag     tag to index
     * @throws Exception any exception
     */
    public void put(Tag tag) throws Exception {
        ObjectNode node = marshaller.serialize(tag);
        String json = mapper.writeValueAsString(node);

        client.execute(new Index.Builder(json)
                .index("munch")
                .type("tag")
                .id(tag.getId())
                .build());
    }

    /**
     * @param type data type to delete before
     * @param key  key of data type
     * @throws Exception exception for deletion
     */
    public void delete(String type, String key) throws Exception {
        client.execute(new Delete.Builder(key)
                .index("munch")
                .type(type)
                .build());
    }
}
