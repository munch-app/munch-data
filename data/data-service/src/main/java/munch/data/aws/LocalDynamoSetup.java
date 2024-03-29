package munch.data.aws;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.io.Resources;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import munch.data.service.TagService;
import munch.data.tag.Tag;
import munch.restful.core.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

/**
 * Created by: Fuxing
 * Date: 25/7/18
 * Time: 1:02 AM
 * Project: catalyst
 */
@SuppressWarnings("UnstableApiUsage")
@Singleton
public final class LocalDynamoSetup {
    private static final Logger logger = LoggerFactory.getLogger(LocalDynamoSetup.class);
    private static final ProvisionedThroughput THROUGHPUT = new ProvisionedThroughput().withReadCapacityUnits(1000L).withWriteCapacityUnits(1000L);

    private final AmazonDynamoDB amazonDynamoDB;
    private final TagService tagService;

    @Inject
    public LocalDynamoSetup(AmazonDynamoDB amazonDynamoDB, TagService tagService) {
        this.amazonDynamoDB = amazonDynamoDB;
        this.tagService = tagService;
    }

    public void setup() throws InterruptedException, IOException {
        addMapped();
        addTags();
    }

    private void addMapped() throws InterruptedException {
        for (Config config : ConfigFactory.load().getConfigList("persistence.mappings")) {
            String tableName = config.getString("tableName");
            String dataKey = config.getString("dataKey");

            create(tableName, request -> {
                request.withAttributeDefinitions(
                        new AttributeDefinition().withAttributeName(dataKey).withAttributeType(ScalarAttributeType.S)
                );

                request.withKeySchema(
                        new KeySchemaElement().withAttributeName(dataKey).withKeyType(KeyType.HASH)
                );
            });
        }
    }

    private void addTags() throws IOException {
        URL tags = Resources.getResource("samples/tags.json.lfs");
        JsonNode json = JsonUtils.objectMapper.readTree(tags);

        for (JsonNode node : json) {
            Tag result = tagService.put(JsonUtils.toObject(node, Tag.class));
            logger.info("LocalTagSetup: {}", result);
        }
    }

    @SuppressWarnings("Duplicates")
    private void create(String tableName, Consumer<CreateTableRequest> consumer) throws InterruptedException {
        CreateTableRequest request = new CreateTableRequest()
                .withTableName(tableName)
                .withProvisionedThroughput(THROUGHPUT);

        consumer.accept(request);

        TableUtils.createTableIfNotExists(amazonDynamoDB, request);
        TableUtils.waitUntilActive(amazonDynamoDB, tableName);
    }
}
