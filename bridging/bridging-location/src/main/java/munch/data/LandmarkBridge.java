package munch.data;

import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableRecord;
import munch.data.client.LandmarkClient;
import munch.data.location.Landmark;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 5/6/18
 * Time: 10:37 AM
 * Project: munch-data
 */
@Singleton
public final class LandmarkBridge extends AirtableBridge<Landmark> {
    private static final Logger logger = LoggerFactory.getLogger(LandmarkBridge.class);

    private final LandmarkClient client;

    @Inject
    public LandmarkBridge(AirtableApi airtableApi, LandmarkClient client) {
        super(logger, airtableApi.base("appERO4wuQ5oJSTxO").table("Landmark"), client::list);
        this.client = client;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(6);
    }

    @Override
    protected void processServer(Landmark data) {
        List<AirtableRecord> records = table.find("landmarkId", data.getLandmarkId());
        if (records.size() == 1) return;
        if (records.size() > 1) throw new IllegalStateException("More then 1 Landmark with the same id.");

        // No Records found: Delete
        client.delete(data.getLandmarkId());
    }

    @Override
    protected void processAirtable(AirtableRecord record, Landmark updated) {
        String landmarkId = record.getField("landmarkId").asText();
        if (StringUtils.isNotBlank(landmarkId)) {
            // Update if Changed
            Landmark landmark = client.get(landmarkId);
            if (updated.equals(landmark)) return;
            client.put(updated);
        } else {
            // Create New
            Landmark posted = client.post(updated);
            AirtableRecord patch = new AirtableRecord();
            patch.setId(record.getId());
            patch.putField("landmarkId", posted.getLandmarkId());
            patch.putFieldDate("updatedMillis", posted.getUpdatedMillis());
            patch.putFieldDate("createdMillis", posted.getCreatedMillis());
            table.patch(patch);
        }
    }

    @Override
    protected Landmark parse(AirtableRecord record) {
        Landmark landmark = new Landmark();
        landmark.setLandmarkId(StringUtils.trimToNull(record.getField("landmarkId").asText()));
        landmark.setName(StringUtils.trimToNull(record.getField("name").asText()));

        String type = record.getField("type").asText();
        if (type == null) return null;
        landmark.setType(Landmark.Type.valueOf(type));

        landmark.setLocation(new Location());
        landmark.getLocation().setCity(StringUtils.trimToNull(record.getField("location.city").asText()));
        landmark.getLocation().setCountry(StringUtils.trimToNull(record.getField("location.country").asText()));
        landmark.getLocation().setLatLng(StringUtils.trimToNull(record.getField("location.latLng").asText()));

        if (landmark.getLocation().getCity() == null) return null;
        if (landmark.getLocation().getCountry() == null) return null;
        if (landmark.getLocation().getLatLng() == null) return null;
        return landmark;
    }
}
