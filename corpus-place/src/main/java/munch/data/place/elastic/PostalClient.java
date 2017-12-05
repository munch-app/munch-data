package munch.data.place.elastic;

import catalyst.utils.iterators.PaginationIterator;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 5/12/2017
 * Time: 10:55 PM
 * Project: munch-data
 */
@Singleton
public final class PostalClient extends ElasticClient {

    @Inject
    public PostalClient() {
        super("postal");
    }

    public Iterator<ElasticPlace> search(String postal) {
        return new PaginationIterator<>(from -> search(postal, from, 50));
    }

    public List<ElasticPlace> search(String postal, int from, int size) {
        ObjectNode root = mapper.createObjectNode();
        root.put("from", from)
                .put("size", size)
                .putObject("query")
                .putObject("bool")
                .putObject("filter")
                .putObject("term")
                .put("postal", postal);

        return search(root);
    }
}
