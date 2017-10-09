package munch.data.place.elastic;

import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.indices.CreateIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(ElasticMapping.class);

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
        sleep(1500);
        createIndex();
        sleep(2000);
    }

    /**
     * Create index for /munch/place
     *
     * @throws IOException io exception
     */
    public void createIndex() throws IOException {
        logger.info("Creating index");
        URL url = Resources.getResource("search-index.json");
        String json = Resources.toString(url, Charset.forName("UTF-8"));
        JestResult result = client.execute(new CreateIndex.Builder("corpus").settings(json).build());
        logger.info("Created index result: {}", result.getJsonString());
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
