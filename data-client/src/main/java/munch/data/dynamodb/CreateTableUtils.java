package munch.data.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.google.common.collect.ImmutableList;

/**
 * Created by: Fuxing
 * Date: 12/10/2017
 * Time: 7:56 AM
 * Project: munch-data
 */
public class CreateTableUtils {
    private final AmazonDynamoDB dynamoDB;

    public CreateTableUtils(AmazonDynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    public void createTable(String tableName, String hashKey) throws InterruptedException {
        CreateTableRequest request = new CreateTableRequest();
        request.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L));
        request.setTableName(tableName);

        request.setAttributeDefinitions(ImmutableList.of(
                new AttributeDefinition(hashKey, ScalarAttributeType.S)
        ));

        request.setKeySchema(ImmutableList.of(
                new KeySchemaElement(hashKey, KeyType.HASH))
        );

        // Create table if not already exists & wait
        TableUtils.createTableIfNotExists(dynamoDB, request);
        TableUtils.waitUntilActive(dynamoDB, tableName);
    }

    public void createTable(String tableName, String hashKey, String sortKey) throws InterruptedException {
        CreateTableRequest request = new CreateTableRequest();
        request.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L));
        request.setTableName(tableName);

        request.setAttributeDefinitions(ImmutableList.of(
                new AttributeDefinition(hashKey, ScalarAttributeType.S),
                new AttributeDefinition(sortKey, ScalarAttributeType.S)
        ));

        request.setKeySchema(ImmutableList.of(
                new KeySchemaElement(hashKey, KeyType.HASH),
                new KeySchemaElement(sortKey, KeyType.RANGE)
        ));

        // Create table if not already exists & wait
        TableUtils.createTableIfNotExists(dynamoDB, request);
        TableUtils.waitUntilActive(dynamoDB, tableName);
    }
}
