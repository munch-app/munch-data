package munch.search.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.searchbox.client.JestClient;
import io.searchbox.core.Delete;
import io.searchbox.core.DeleteByQuery;
import io.searchbox.core.Index;
import munch.data.Location;
import munch.data.Place;
import munch.data.Tag;

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
     * Index a tag by putting it into elastic search
     *
     * @param tag     tag to index
     * @param cycleNo cycle long in millis
     * @throws Exception any exception
     */
    public void put(Tag tag, long cycleNo) throws Exception {
        ObjectNode node = marshaller.serialize(tag, cycleNo);
        String json = mapper.writeValueAsString(node);

        client.execute(new Index.Builder(json)
                .index("munch")
                .type("tag")
                .id(tag.getId())
                .build());
    }

    /**
     * Index a place by putting it into elastic search
     *
     * @param place   place to index
     * @param cycleNo cycle long in millis
     * @throws Exception any exception
     */
    public void put(Place place, long cycleNo) throws Exception {
        ObjectNode node = marshaller.serialize(place, cycleNo);
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
     * @param cycleNo  cycle long in millis
     * @throws Exception any exception
     */
    public void put(Location location, long cycleNo) throws Exception {
        ObjectNode node = marshaller.serialize(location, cycleNo);
        String json = mapper.writeValueAsString(node);

        client.execute(new Index.Builder(json)
                .index("munch")
                .type("location")
                .id(location.getId())
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

    /**
     * @param type    data type to delete before
     * @param cycleNo cycle long in millis
     * @throws Exception exception for deletion
     */
    public void deleteBefore(String type, long cycleNo) throws Exception {
        ObjectNode root = mapper.createObjectNode();
        root.putObject("query")
                .putObject("range")
                .putObject("cycleNo")
                .put("lt", cycleNo);

        String json = mapper.writeValueAsString(root);
        client.execute(new DeleteByQuery.Builder(json)
                .addIndex("munch")
                .addType(type)
                .setParameter("conflicts", "proceed")
                .build());
    }
}
