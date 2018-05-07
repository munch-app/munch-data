package munch.data.place.graph.seeder;

import com.fasterxml.jackson.databind.JsonNode;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableRecord;
import corpus.data.CorpusData;
import corpus.engine.AbstractEngine;
import corpus.field.PlaceKey;
import munch.data.clients.PlaceClient;
import munch.data.structure.Place;
import munch.restful.core.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.*;

/**
 * Created by: Fuxing
 * Date: 1/4/2018
 * Time: 2:20 AM
 * Project: munch-data
 */
@Singleton
public final class DecayAirtableCorpus extends AbstractEngine<Object> {
    private static final Logger logger = LoggerFactory.getLogger(DecayAirtableCorpus.class);

    private final AirtableApi.Table table;
    private final PlaceClient placeClient;
    private Map<String, AirtableRecord> decayRecords;

    @Inject
    public DecayAirtableCorpus(AirtableApi airtableApi, PlaceClient placeClient) {
        super(logger);
        this.table = airtableApi.base("appICfPRcuTAzDPGd").table("Decaying");
        this.placeClient = placeClient;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(3);
    }

    @Override
    protected void doCycle(long cycleNo, Iterator<Object> iterator) {
        this.decayRecords = new HashMap<>();

        doCycleAirtable(cycleNo);
        doCycleDecaying(cycleNo);

        // Delete all that is not updated anymore
        decayRecords.forEach((placeId, record) -> {
            table.delete(record.getId());
            sleep(3000);
        });
    }

    private void doCycleAirtable(long cycleNo) {
        Iterator<AirtableRecord> iterator = table.select(Duration.ofSeconds(3));
        while (iterator.hasNext()) {
            checkInterrupted();
            process(iterator.next());
        }
    }

    /**
     * @param record to process
     */
    private void process(AirtableRecord record) {
        String placeId = record.getField("Place.id").asText();
        Date completeDate = record.getFieldDate("Decay Complete Date");
        String status = record.getField("Status").asText(null);

        Objects.requireNonNull(placeId);
        Objects.requireNonNull(completeDate);

        if (status != null) {
            if (status.equalsIgnoreCase("closed")) {
                resolve(placeId, true);
                table.delete(record.getId());
                return;
            } else if (status.equalsIgnoreCase("open")) {
                resolve(placeId, false);
                table.delete(record.getId());
                return;
            }
        }

        if (completeDate.before(new Date())) {
            table.delete(record.getId());
            return;
        }

        decayRecords.put(placeId, record);
    }

    private void doCycleDecaying(long cycleNo) {
        Iterator<CorpusData> iterator = corpusClient.list("Sg.Munch.Place.Decaying");
        while (iterator.hasNext()) {
            checkInterrupted();
            process(iterator.next());
        }
    }

    private void process(CorpusData data) {
        // Resolve Expired
        String placeId = data.getCorpusKey();
        String endMillis = DecayingKey.endMillis.getValueOrThrow(data);
        Date completeDate = new Date(Long.parseLong(endMillis));

        if (!catalystClient.hasCorpus(data.getCatalystId(), "Sg.Munch.Place")) {
            return;
        }

        if (completeDate.before(new Date())) {
            resolve(placeId, true);
            return;
        }

        Place place = placeClient.get(placeId);
        if (place == null) return;

        Map<String, JsonNode> fields = new HashMap<>();
        fields.put("Place.id", JsonUtils.toTree(placeId));
        fields.put("Decaying.name", JsonUtils.toTree(DecayingKey.name.getValue(data)));
        fields.put("Decay Complete Date", JsonUtils.toTree(AirtableApi.DATE_FORMAT.format(completeDate)));
        fields.put("UpdatedDate", JsonUtils.toTree(AirtableApi.DATE_FORMAT.format(place.getUpdatedDate())));

        fields.put("Place.name", JsonUtils.toTree(place.getName()));
        fields.put("Place.phone", JsonUtils.toTree(place.getPhone()));
        fields.put("Place.website", JsonUtils.toTree(place.getWebsite()));
        fields.put("Place.Location.address", JsonUtils.toTree(place.getLocation().getAddress()));
        fields.put("Place.Location.neighbourhood", JsonUtils.toTree(place.getLocation().getNeighbourhood()));
        fields.put("Place.Location.postal", JsonUtils.toTree(place.getLocation().getPostal()));

        AirtableRecord record = decayRecords.get(data.getCorpusKey());
        if (record != null) {
            // Patch
            record.setFields(fields);
            table.patch(record);
            decayRecords.remove(placeId);
        } else {
            // Post
            record = new AirtableRecord();
            record.setFields(fields);
            table.post(record);
            sleep(3000);
        }
    }

    @Override
    protected Iterator<Object> fetch(long cycleNo) {
        return Collections.emptyIterator();
    }

    @Override
    protected void process(long cycleNo, Object data, long processed) {
        throw new UnsupportedOperationException();
    }

    public void resolve(String placeId, boolean decayed) {
        CorpusData data = new CorpusData("", placeId, 0);
        data.setCatalystId(placeId);
        data.put(PlaceKey.id, placeId);

        if (decayed) {
            corpusClient.put("Sg.Munch.Place.Decaying.Decayed", placeId, data);
            corpusClient.delete("Sg.Munch.Place.Decaying.Stop", placeId);
        } else {
            corpusClient.put("Sg.Munch.Place.Decaying.Stop", placeId, data);
            corpusClient.delete("Sg.Munch.Place.Decaying.Decayed", placeId);
        }
        corpusClient.delete("Sg.Munch.Place.Decaying", placeId);
    }
}
