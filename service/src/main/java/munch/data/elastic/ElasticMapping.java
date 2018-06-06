package munch.data.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.mapping.GetMapping;
import io.searchbox.indices.mapping.PutMapping;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * For validation of Mappings
 * Created By: Fuxing Loh
 * Date: 10/3/2017
 * Time: 11:22 PM
 * Project: munch-core
 */
@Singleton
public final class ElasticMapping {
    public static final String INDEX_NAME = "munch5";
    public static final String TABLE_NAME = "Data4";

    private static final Logger logger = LoggerFactory.getLogger(ElasticMapping.class);
    private static final ObjectMapper mapper = JsonUtils.objectMapper;

    private final JestClient client;

    @Inject
    public ElasticMapping(JestClient client) {
        this.client = client;
    }

    /**
     * Does 2 things
     * 1. Try to create mappings if don't exist
     * 2. Validate mappings
     *
     * @throws RuntimeException if failed to create or validate
     */
    public void tryCreate() throws RuntimeException, IOException {
        sleep(1000);
        logger.info("Validating Index for endpoint /munch");
        JsonNode index = getIndex();

        // Index don't exist; hence create and revalidate
        // Note: index updating is complex
        if (index == null) {
            createIndex();
            sleep(5000);
            index = getIndex();
        } else {
            JsonNode data = getExpectedMapping().path("mappings").path(TABLE_NAME);
            JestResult execute = client.execute(new PutMapping.Builder(ElasticMapping.INDEX_NAME, TABLE_NAME, mapper.writeValueAsString(data)).build());
            logger.info(execute.getJsonString());

            if (execute.getResponseCode() != 200) {
                throw new RuntimeException("elastic-index.json is different from server");
            }
        }

        logger.info("Index: {}", index);
    }

    /**
     * Index of corpus
     *
     * @return null if not found
     */
    @Nullable
    private JsonNode getIndex() throws IOException {
        JestResult result = client.execute(new GetMapping.Builder()
                .addIndex(ElasticMapping.INDEX_NAME)
                .build());
        JsonNode node = mapper.readTree(result.getJsonString());
        String type = node.path("error").path("type").asText(null);
        if (StringUtils.equalsIgnoreCase(type, "index_not_found_exception")) {
            logger.info("Index not found");
            return null;
        }
        return node;
    }

    /**
     * Create index for /munch/place
     *
     * @throws IOException io exception
     */
    public void createIndex() throws IOException {
        logger.info("Creating index");
        URL url = Resources.getResource("elastic-index.json");
        String json = Resources.toString(url, Charset.forName("UTF-8"));
        JestResult result = client.execute(new CreateIndex.Builder(ElasticMapping.INDEX_NAME).settings(json).build());
        logger.info("Created index result: {}", result.getJsonString());
    }

    public JsonNode getExpectedMapping() throws IOException {
        URL url = Resources.getResource("elastic-index.json");
        String json = Resources.toString(url, Charset.forName("UTF-8"));
        return mapper.readTree(json);
    }

    /**
     * Wait for es to apply change
     * sleep for 2 seconds
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
