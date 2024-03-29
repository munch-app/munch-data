package munch.data;

import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableRecord;
import munch.data.airtable.AirtableBridge;
import munch.data.airtable.AirtableUtils;
import munch.data.airtable.SpatialUtils;
import munch.data.client.AreaClient;
import munch.data.location.Area;
import munch.data.location.Location;
import munch.file.ImageClient;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 5/6/18
 * Time: 10:37 AM
 * Project: munch-data
 */
@Singleton
public final class AreaBridge extends AirtableBridge<Area> {
    private static final Logger logger = LoggerFactory.getLogger(AreaBridge.class);

    private final AreaClient areaClient;
    private final ImageClient imageClient;

    @Inject
    public AreaBridge(AirtableApi airtableApi, AreaClient areaClient, ImageClient imageClient) {
        super(logger, airtableApi.base("appERO4wuQ5oJSTxO").table("Area"), areaClient::iterator);
        this.areaClient = areaClient;
        this.imageClient = imageClient;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(6);
    }

    @Override
    protected void processServer(Area data) {
        List<AirtableRecord> records = table.find("areaId", data.getAreaId());
        if (records.size() == 1) return;
        if (records.size() > 1) throw new IllegalStateException("More then 1 Area with the same id.");

        // No Records found: Delete
        areaClient.delete(data.getAreaId());
    }

    @Override
    protected void processAirtable(AirtableRecord record, Area updated) {
        String areaId = record.getField("areaId").asText();
        if (StringUtils.isNotBlank(areaId)) {
            // Update if Changed
            Long count = areaClient.countPlaces(areaId);
            updated.setCounts(new Area.Counts());
            updated.getCounts().setTotal(count != null ? count : 0);
            if (updated.equals(areaClient.get(areaId))) return;

            AirtableRecord patch = new AirtableRecord();
            patch.setId(record.getId());
            patch.putField("counts.total", updated.getCounts().getTotal());
            // Patch to Airtable & Client
            table.patch(patch);
            areaClient.put(updated);
        } else {
            if (updated == null) {
                logger.warn("Failed to Parse Area, {}", record.getField("name").asText());
                return;
            }
            // Create New
            Area posted = areaClient.post(updated);
            AirtableRecord patch = new AirtableRecord();
            patch.setId(record.getId());
            patch.putField("areaId", posted.getAreaId());
            patch.putFieldDate("updatedMillis", posted.getUpdatedMillis());
            patch.putFieldDate("createdMillis", posted.getCreatedMillis());
            table.patch(patch);
        }
    }

    @Override
    protected Area parse(AirtableRecord record) {
        Area area = new Area();
        Location location = parseLocation(record);
        if (location == null) return null;
        area.setLocation(location);

        String type = record.getField("type").asText();
        if (type == null) return null;
        area.setType(Area.Type.valueOf(type));

        area.setName(StringUtils.trimToNull(record.getField("name").asText()));
        area.setNames(AirtableUtils.multiLineToSet(record.getField("names")));
        area.setAreaId(StringUtils.trimToNull(record.getField("areaId").asText()));
        area.setWebsite(StringUtils.trimToNull(record.getField("website").asText()));
        area.setDescription(StringUtils.trimToNull(record.getField("description").asText()));

        area.setImages(AirtableUtils.getImages(imageClient, record.getField("images")));
        area.setHours(AirtableUtils.parseHours(record.getField("hours")));
        area.setLocationCondition(parseLocationCondition(record));
        return area;
    }

    protected Area.LocationCondition parseLocationCondition(AirtableRecord record) {
        Set<String> postcodes = JsonUtils.toSet(record.getField("locationCondition.postcodes"), String.class);
        Set<String> unitNumbers = JsonUtils.toSet(record.getField("locationCondition.unitNumbers"), String.class);
        if (postcodes == null) postcodes = Set.of();
        if (unitNumbers == null) unitNumbers = Set.of();

        if (!postcodes.isEmpty() || !unitNumbers.isEmpty()) {
            Area.LocationCondition condition = new Area.LocationCondition();
            condition.setPostcodes(postcodes);
            condition.setUnitNumbers(unitNumbers);
            return condition;
        }
        return null;
    }

    @Nullable
    protected Location parseLocation(AirtableRecord record) {
        Location location = new Location();

        location.setLatLng(StringUtils.trimToNull(record.getField("location.latLng").asText()));
        location.setCity(StringUtils.trimToNull(record.getField("location.city").asText()));
        location.setCountry(StringUtils.trimToNull(record.getField("location.country").asText()));

        Location.Polygon polygon = new Location.Polygon();
        polygon.setPoints(SpatialUtils.wktToPoints(record.getField("location.polygon").asText()));
        location.setPolygon(polygon);

        location.setAddress(StringUtils.trimToNull(record.getField("location.address").asText()));
        location.setPostcode(StringUtils.trimToNull(record.getField("location.postcode").asText()));

        if (location.getLatLng() == null) return null;
        if (location.getPolygon() == null || location.getPolygon().getPoints() == null) return null;
        if (location.getCity() == null) return null;
        if (location.getCountry() == null) return null;
        return location;
    }
}
