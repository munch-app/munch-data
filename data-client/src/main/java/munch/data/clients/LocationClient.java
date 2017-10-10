package munch.data.clients;

import com.fasterxml.jackson.databind.JsonNode;
import munch.data.elastic.ElasticClient;
import munch.data.elastic.ElasticIndex;
import munch.data.elastic.ElasticMarshaller;
import munch.data.structure.Location;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 10/10/2017
 * Time: 2:16 AM
 * Project: munch-data
 */
@Singleton
public class LocationClient {
    private final ElasticIndex elasticIndex;
    private final ElasticClient elasticClient;
    private final ElasticMarshaller marshaller;

    @Inject
    public LocationClient(ElasticIndex elasticIndex, ElasticClient elasticClient, ElasticMarshaller marshaller) {
        this.elasticIndex = elasticIndex;
        this.elasticClient = elasticClient;
        this.marshaller = marshaller;
    }

    /**
     * @param text text
     * @param size size of location to suggest
     * @return list of Location
     */
    public List<Location> suggest(String text, int size) throws IOException {
        JsonNode results = elasticClient.suggest("Location", text, null, size);
        return marshaller.deserializeList(results);
    }

    public Location get(String id) throws IOException {
        return elasticIndex.get("Location", id);
    }


    public void put(Location location) throws IOException {
        elasticIndex.put(location);
    }

    public void delete(String id) throws IOException {
        elasticIndex.delete("Location", id);
    }
}