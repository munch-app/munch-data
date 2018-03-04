package munch.data.place;

import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableRecord;
import munch.data.structure.Place;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Created by: Fuxing
 * Date: 4/3/2018
 * Time: 3:17 AM
 * Project: munch-data
 */
public final class AwardCollection {
    private final AirtableApi.Table table;

    private final long uniqueId;
    private final String tableName;
    private final String collectionName;
    private final List<AwardPlace> awardPlaces;

    public AwardCollection(AirtableApi.Table table, long collectionId, String tableName, String collectionName, List<AwardPlace> awardPlaces) {
        this.table = table;
        this.uniqueId = collectionId;
        this.tableName = tableName;
        this.collectionName = collectionName;
        this.awardPlaces = awardPlaces;
    }

    public long getCollectionId() {
        return uniqueId;
    }

    public String getTableName() {
        return tableName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public List<AwardPlace> getAwardPlaces() {
        return awardPlaces;
    }

    public class AwardPlace {
        public static final String STATUS_LINKED = "Linked";
        public static final String STATUS_CONFLICT = "Conflict";

        private final String airtableId;

        private final long awardId;
        private final String name;
        private final String address;

        private String status;
        private String munchId;

        public AwardPlace(String airtableId, long awardId, String name, String address) {
            this.airtableId = airtableId;
            this.awardId = awardId;
            this.name = name;
            this.address = address;
        }

        public String getAirtableId() {
            return airtableId;
        }

        public long getAwardId() {
            return awardId;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public String getMunchId() {
            return munchId;
        }

        public void setMunchId(String munchId) {
            this.munchId = munchId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void tryLink(BiFunction<String, String, Place> mapper) {
            if (StringUtils.isNotBlank(status)) return;

            Place place = mapper.apply(getName(), getAddress());
            AirtableRecord record = new AirtableRecord();
            record.setId(getAirtableId());

            if (place != null) {
                setMunchId(place.getId());
                setStatus(STATUS_LINKED);

                record.setFields(Map.of(
                        "Status", JsonUtils.toTree(getStatus()),
                        "Place.id", JsonUtils.toTree(getMunchId())
                ));
            } else {
                setStatus(STATUS_CONFLICT);

                record.setFields(Map.of(
                        "Status", JsonUtils.toTree(getStatus())
                ));
            }
            table.patch(record);
        }
    }
}
