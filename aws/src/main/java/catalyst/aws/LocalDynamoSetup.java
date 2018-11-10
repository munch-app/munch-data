package catalyst.aws;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Consumer;

/**
 * Created by: Fuxing
 * Date: 25/7/18
 * Time: 1:02 AM
 * Project: catalyst
 */
@Singleton
public final class LocalDynamoSetup {
    private static final ProvisionedThroughput THROUGHPUT = new ProvisionedThroughput().withReadCapacityUnits(1000L).withWriteCapacityUnits(1000L);
    private final AmazonDynamoDB amazonDynamoDB;

    @Inject
    public LocalDynamoSetup(AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
    }

    public void setup() throws InterruptedException {

    }

    private void create(String tableName, Consumer<CreateTableRequest> consumer) throws InterruptedException {
        CreateTableRequest request = new CreateTableRequest()
                .withTableName(tableName)
                .withProvisionedThroughput(THROUGHPUT);

        consumer.accept(request);

        TableUtils.createTableIfNotExists(amazonDynamoDB, request);
        TableUtils.waitUntilActive(amazonDynamoDB, tableName);
    }
}
