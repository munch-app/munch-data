package munch.data.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterators;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import io.searchbox.params.Parameters;
import munch.data.exception.ClusterBlockException;
import munch.data.exception.ElasticException;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
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
    private final ElasticSerializer marshaller;

    /**
     * https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/java-docs-index.html
     *
     * @param client     injected rest client
     * @param marshaller to marshal json
     * @throws RuntimeException if ElasticSearchMapping validation failed
     */
    @Inject
    public ElasticIndex(JestClient client, ElasticSerializer marshaller) {
        this.client = client;
        this.marshaller = marshaller;
    }

    /**
     * Index a tag by putting it into elastic search
     *
     * @param object elastic object to persist
     * @throws ElasticException wrapped exception
     */
    public void put(ElasticObject object) throws ElasticException {
        try {
            String json = mapper.writeValueAsString(marshaller.serialize(object));
            DocumentResult result = client.execute(new Index.Builder(json)
                    .index(ElasticMapping.INDEX_NAME)
                    .type(ElasticMapping.TABLE_NAME)
                    .id(object.getElasticId())
                    .build());

            validateResult(true, result);
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }

    /**
     * @param dataType data type to get
     * @param dataId   id of data
     * @param <T>      Data Type
     * @return data if exists
     */
    @Nullable
    public <T extends ElasticObject> T get(DataType dataType, String dataId) {
        try {
            Get get = new Get.Builder(ElasticMapping.INDEX_NAME, ElasticObject.createElasticId(dataType, dataId))
                    .type(ElasticMapping.TABLE_NAME)
                    .build();
            DocumentResult result = client.execute(get);
            validateResult(false, result);

            return ElasticUtils.deserialize(mapper.readTree(result.getJsonString()));
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }

    /**
     * @param dataType data type to delete before
     * @param dataId   key of data type
     * @throws ElasticException wrapped exception
     */
    public void delete(DataType dataType, String dataId) {
        try {
            DocumentResult result = client.execute(new Delete.Builder(ElasticObject.createElasticId(dataType, dataId))
                    .index(ElasticMapping.INDEX_NAME)
                    .type(ElasticMapping.TABLE_NAME)
                    .build());

            validateResult(false, result);
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }

    /**
     * @param dataType data type to scroll
     * @param timeout  timeout in elastic time unit e.g. 1m
     * @param size     size per batch
     * @param <T>      data type
     * @return Iterator of batch Data
     */
    public <T extends ElasticObject> Iterator<List<T>> scroll(String indexName, DataType dataType, String timeout, int size) {
        ObjectNode node = JsonUtils.createObjectNode();
        node.put("size", size);
        node.putObject("query").putObject("term").put("dataType", dataType.name());


        Search search = new Search.Builder(JsonUtils.toString(node))
                .addIndex(indexName)
                .setParameter(Parameters.SCROLL, timeout)
                .build();

        try {
            return new Iterator<>() {
                JsonNode result = mapper.readTree(client.execute(search).getJsonString());
                List<T> nextList = ElasticUtils.deserializeList(result.path("hits").path("hits"));

                @Override
                public boolean hasNext() {
                    if (nextList == null) {
                        try {
                            SearchScroll scroll = new SearchScroll.Builder(result.path("_scroll_id").asText(), timeout).build();
                            result = mapper.readTree(client.execute(scroll).getJsonString());
                            nextList = ElasticUtils.deserializeList(result.path("hits").path("hits"));

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
     * @param dataType type: Place, Container, Location, Tag
     * @param timeout  timeout different each batched request
     * @param <T>      T data type
     * @return Iterator of that Data
     */
    public <T extends ElasticObject> Iterator<T> scroll(DataType dataType, String timeout) {
        return scroll(dataType, timeout, 20);
    }

    public <T extends ElasticObject> Iterator<T> scroll(DataType dataType, String timeout, int size) {
        Iterator<List<T>> listIterator = scroll(ElasticMapping.INDEX_NAME, dataType, timeout, size);
        return Iterators.concat(Iterators.transform(listIterator, List::iterator));
    }

    private static void validateResult(boolean validateNotFound, DocumentResult result) {
        if (result.getErrorMessage() != null) {
            JsonNode jsonNode = JsonUtils.readTree(result.getJsonString());
            String errorType = jsonNode.path("error").path("type").asText();
            if (StringUtils.equals(errorType, "cluster_block_exception")) {
                throw new ClusterBlockException();
            }

            if (jsonNode.path("result").asText("").equals("not_found") || !jsonNode.path("found").asBoolean()) {
                if (validateNotFound) {
                    logger.warn("{}", jsonNode);
                    throw new ElasticException(404, "Failed to put/delete/get object.");
                }
            } else {
                logger.warn("{}", jsonNode);
                throw new ElasticException("Failed to put/delete/get object.");
            }
        }
    }
}
