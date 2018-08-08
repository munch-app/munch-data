package munch.data.service;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 4/6/18
 * Time: 5:52 PM
 * Project: munch-data
 */
@Singleton
public final class PersistenceMapping {
    private final Map<String, Mapping> mappings;

    @Inject
    public PersistenceMapping(DynamoDB dynamoDB) {
        this.mappings = new HashMap<>();

        for (Config config : ConfigFactory.load().getConfigList("persistence.mappings")) {
            Mapping mapping = new Mapping();
            mapping.setDataType(config.getString("dataType"));
            mapping.setDataKey(config.getString("dataKey"));
            mapping.setTableName(config.getString("tableName"));
            mapping.setTable(dynamoDB.getTable(mapping.getTableName()));

            mappings.put(mapping.getDataType(), mapping);
        }
    }

    /**
     * @param clazz Class.getSimpleName() = dataType
     * @return Table if found
     * @throws IllegalArgumentException if table not found
     */
    public Mapping getMapping(Class clazz) throws IllegalArgumentException {
        return getMapping(clazz.getSimpleName());
    }

    public Mapping getMapping(String dataType) throws IllegalArgumentException {
        Mapping mapping = mappings.get(dataType);
        if (mapping != null) return mapping;
        throw new IllegalArgumentException(dataType + " table do not exist.");
    }

    /**
     * Persistence Mapping
     */
    public static class Mapping {
        private String dataType;
        private String dataKey;
        private String tableName;

        private Table table;

        public String getDataType() {
            return dataType;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }

        public String getDataKey() {
            return dataKey;
        }

        public void setDataKey(String dataKey) {
            this.dataKey = dataKey;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public Table getTable() {
            return table;
        }

        public void setTable(Table table) {
            this.table = table;
        }
    }
}
