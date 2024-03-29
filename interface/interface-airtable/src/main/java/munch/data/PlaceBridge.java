package munch.data;

import com.google.common.collect.Iterators;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableRecord;
import corpus.engine.AbstractEngine;
import munch.data.client.PlaceClient;
import munch.data.place.AirtablePlaceMapper;
import munch.data.place.Place;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 6/6/18
 * Time: 3:00 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceBridge extends AbstractEngine<Object> {
    private static final Logger logger = LoggerFactory.getLogger(PlaceBridge.class);

    private final PlaceClient placeClient;

    private final AirtableApi.Table placeTable;
    private final AirtablePlaceMapper placeMapper;

    @Inject
    public PlaceBridge(AirtableApi api, PlaceClient placeClient, AirtablePlaceMapper placeMapper) {
        super(logger);
        this.placeClient = placeClient;
        this.placeTable = api.base("appDcx5b3vgkhcYB5").table("Place");
        this.placeMapper = placeMapper;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(8);
    }

    @Override
    protected Iterator<Object> fetch(long cycleNo) {
        return Iterators.concat(placeTable.select(), placeClient.iterator());
    }

    @Override
    protected void process(long cycleNo, Object object, long processed) {
        if (object instanceof Place) {
            processServer((Place) object);
        } else {
            // From Airtable, check if need to be deleted
            AirtableRecord record = (AirtableRecord) object;
            Place place = placeClient.get(record.getField("placeId").asText());
            if (place == null) {
                placeTable.delete(record.getId());
            }
        }

        if (processed % 1000 == 0) logger.info("Processed: {}", processed);
    }

    protected void processServer(Place place) {
        // From Server, Check if Place need to be posted or patched
        List<AirtableRecord> records = placeTable.find("placeId", place.getPlaceId());
        sleep(100);

        if (records.size() == 0) {
            // Posted
            placeTable.post(placeMapper.parse(place));
            sleep(100);
            return;
        }

        // Patched
        AirtableRecord record = records.get(0);
        AirtableRecord patchRecord = placeMapper.parse(place);
        patchRecord.setId(record.getId());

        if (equals(place, record)) return;
        placeTable.patch(patchRecord);
        sleep(100);

        for (int i = 1; i < records.size(); i++) {
            placeTable.delete(records.get(i).getId());
        }
    }


    private static boolean equals(Place place, AirtableRecord record) {
        return record.getFieldDate("updatedMillis").getTime() == place.getUpdatedMillis();
    }
}
