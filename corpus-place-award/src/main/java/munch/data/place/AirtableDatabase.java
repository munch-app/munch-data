package munch.data.place;

import com.google.common.collect.Iterators;
import corpus.airtable.AirtableApi;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 4/3/2018
 * Time: 2:55 AM
 * Project: munch-data
 */
@Singleton
public final class AirtableDatabase {

    private final AirtableApi.Base airtableBase;
    private final AirtableApi.Table indexTable;

    @Inject
    public AirtableDatabase(AirtableApi airtableApi) {
        this.airtableBase = airtableApi.base("appyZ1jrMU3w0G53V");
        this.indexTable = airtableBase.table("Index");
    }

    public Iterator<AwardCollection> list() {
        return Iterators.transform(indexTable.select(Duration.ofMillis(500)), input -> {
            Objects.requireNonNull(input);

            long collectionId = input.getField("Collection Id").asLong();
            String tableName = input.getField("Table Name").asText();
            String collectionName = input.getField("Collection Name").asText();

            AirtableApi.Table table = airtableBase.table(tableName);

            List<AwardCollection.AwardPlace> awardPlaces = new ArrayList<>();
            AwardCollection collection = new AwardCollection(table, collectionId, tableName, collectionName, awardPlaces);

            table.select(Duration.ofMillis(500)).forEachRemaining(record -> {
                long awardId = record.getField("Award Id").asLong();
                String name = record.getField("Place.name").asText();
                String address = record.getField("Place.Location.address").asText();
                if (StringUtils.isAnyBlank(name, address)) return;

                awardPlaces.add(collection.new AwardPlace(record.getId(), awardId, name, address));
            });

            return collection;
        });
    }
}