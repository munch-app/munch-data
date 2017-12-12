package munch.data.clients;

import munch.data.elastic.ElasticIndex;
import munch.data.exceptions.ElasticException;
import munch.data.structure.Tag;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 10/10/2017
 * Time: 2:16 AM
 * Project: munch-data
 */
@Singleton
public class TagClient extends AbstractClient {
    private final ElasticIndex elasticIndex;

    @Inject
    public TagClient(ElasticIndex elasticIndex) {
        this.elasticIndex = elasticIndex;
    }

    public Tag get(String id) throws ElasticException {
        return elasticIndex.get("Tag", id);
    }


    public void put(Tag tag) throws ElasticException {
        elasticIndex.put(tag);
    }

    public void delete(String id) throws ElasticException {
        elasticIndex.delete("Tag", id);
    }
}
