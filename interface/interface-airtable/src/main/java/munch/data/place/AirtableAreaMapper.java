package munch.data.place;

import catalyst.utils.exception.DateCompareUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableRecord;
import munch.data.location.Area;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 19/8/18
 * Time: 11:20 PM
 * Project: munch-data
 */
@Singleton
public final class AirtableAreaMapper {
    private static final Logger logger = LoggerFactory.getLogger(AirtableAreaMapper.class);

    private final AirtableApi.Table areaTable;

    private final Map<String, Pair<String, Area>> areaMap = new HashMap<>();

    @Inject
    public AirtableAreaMapper(AirtableApi api) {
        this.areaTable = api.base("appDcx5b3vgkhcYB5").table("Area");
    }

    public JsonNode mapField(List<Area> areas) {
        ArrayNode fields = JsonUtils.createArrayNode();
        for (Area area: areas) {
            fields.add(areaMap.computeIfAbsent(area.getAreaId(), s -> find(area)).getLeft());
        }
        return fields;
    }

    private Pair<String, Area> find(Area area) {
        List<AirtableRecord> records = areaTable.find("areaId", area.getAreaId());

        AirtableRecord record;
        if (records.size() == 0) record = new AirtableRecord();
        else record = records.get(0);

        for (int i = 1; i < records.size(); i++) {
            // Auto Cleanup
            areaTable.delete(records.get(i).getId());
            sleep();
        }

        if (equals(area, record)) return Pair.of(record.getId(), area);

        record.setFields(new HashMap<>());
        record.putField("areaId", area.getAreaId());
        record.putField("type", area.getType().name());
        record.putField("name", area.getName());
        record.putField("website", area.getWebsite());
        record.putField("description", area.getDescription());

        record.putField("location.address", area.getLocation().getAddress());
        record.putField("location.street", area.getLocation().getStreet());
        record.putField("location.unitNumber", area.getLocation().getUnitNumber());
        record.putField("location.neighbourhood", area.getLocation().getNeighbourhood());

        record.putField("location.city", area.getLocation().getCity());
        record.putField("location.country", area.getLocation().getCountry());
        record.putField("location.postcode", area.getLocation().getPostcode());

        record.putField("location.latLng", area.getLocation().getLatLng());
        record.putFieldDate("createdMillis", area.getCreatedMillis());
        record.putFieldDate("updatedMillis", area.getUpdatedMillis());

        if (record.getId() == null) {
            record = areaTable.post(record);
        } else {
            record = areaTable.patch(record);
        }

        logger.info("Updated Area: {}", area.getAreaId());
        sleep();

        return Pair.of(record.getId(), area);
    }

    private static boolean equals(Area area, AirtableRecord record) {
        if (record.getFields().getOrDefault("updatedMillis", null) == null) return false;
        if (DateCompareUtils.after(record.getFieldDate("updatedMillis").getTime(), Duration.ofDays(3), area.getUpdatedMillis())) return false;
        if (!record.getField("name").asText().equals(area.getName())) return false;
        return true;
    }

    private static void sleep() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
