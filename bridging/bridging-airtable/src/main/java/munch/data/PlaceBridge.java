package munch.data;

import catalyst.utils.exception.DateCompareUtils;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableRecord;
import corpus.engine.AbstractEngine;
import munch.data.client.PlaceClient;
import munch.data.place.Place;
import munch.file.Image;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

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
    private final AirtableMapper airtableMapper;

    @Inject
    public PlaceBridge(AirtableApi api, PlaceClient placeClient, AirtableMapper airtableMapper) {
        super(logger);
        this.placeClient = placeClient;
        this.placeTable = api.base("appDcx5b3vgkhcYB5").table("Place");
        this.airtableMapper = airtableMapper;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(12);
    }

    @Override
    protected Iterator<Object> fetch(long cycleNo) {
        ArrayNode sort = JsonUtils.createArrayNode();
        sort.addObject().put("placeId", "desc");
        return Iterators.concat(placeTable.select(sort), placeClient.iterator());
    }

    @Override
    protected void process(long cycleNo, Object object, long processed) {
        if (object instanceof Place) {
            processServer((Place) object);
        } else {
            // From Airtable, check if need to be deleted
            AirtableRecord record = (AirtableRecord) object;
            Place place = placeClient.get(record.getField("placeId").asText());
            if (place == null) placeTable.delete(record.getId());
        }

        if (processed % 1000 == 0) logger.info("Processed: {}", processed);
    }

    protected void processServer(Place place) {
        // From Server, Check if Place need to be posted or patched
        List<AirtableRecord> records = placeTable.find("placeId", place.getPlaceId());
        sleep(250);

        if (records.size() == 0) {
            // Posted
            placeTable.post(parse(place));
            sleep(250);
            return;
        }
        if (records.size() == 1) {
            // Patched
            AirtableRecord record = records.get(0);
            if (equals(place, record)) return;
            AirtableRecord patchRecord = parse(place);
            patchRecord.setId(record.getId());
            placeTable.patch(patchRecord);
            sleep(250);
            return;
        }

        throw new IllegalStateException("More then 1 Place with the same id.");
    }

    private AirtableRecord parse(Place place) {
        AirtableRecord record = new AirtableRecord();
        record.setFields(new HashMap<>());
        record.putField("placeId", place.getPlaceId());
        record.putField("status", place.getStatus().getType().name());

        record.putField("name", place.getName());
        record.putField("names", Joiner.on("\n").join(place.getNames()));
        record.putField("tags", airtableMapper.mapTagField(place.getTags()));

        record.putField("email", place.getEmail());
        record.putField("phone", place.getPhone());
        record.putField("website", place.getWebsite());
        record.putField("description", place.getDescription());

        record.putField("location.address", place.getLocation().getAddress());
        record.putField("location.street", place.getLocation().getStreet());
        record.putField("location.unitNumber", place.getLocation().getUnitNumber());
        record.putField("location.neighbourhood", place.getLocation().getNeighbourhood());

        record.putField("location.city", place.getLocation().getCity());
        record.putField("location.country", place.getLocation().getCountry());
        record.putField("location.postcode", place.getLocation().getPostcode());

        record.putField("location.latLng", place.getLocation().getLatLng());

        record.putField("menu.url", () -> JsonUtils.toTree(place.getMenu().getUrl()));
        record.putField("price.perPax", () -> JsonUtils.toTree(place.getPrice().getPerPax()));


        record.putField("hours", () -> {
            String hours = place.getHours().stream()
                    .map(h -> h.getDay().name() + ": " + h.getOpen() + "-" + h.getClose())
                    .collect(Collectors.joining("\n"));
            return JsonUtils.toTree(hours);
        });

        record.putField("images", () -> {
            if (place.getImages().isEmpty()) return JsonUtils.createArrayNode();
            ArrayNode array = JsonUtils.createArrayNode();
            for (Image image : place.getImages()) {
                String url = image.getSizes()
                        .stream()
                        .min(Comparator.comparingInt(Image.Size::getWidth))
                        .map(Image.Size::getUrl)
                        .orElse(null);
                if (url == null) continue;
                array.addObject().put("url", url);
            }
            return array;
        });

        // Linked Data

        record.putField("areas", airtableMapper.mapAreaField(place.getAreas()));

        record.putFieldDate("createdMillis", place.getCreatedMillis());
        record.putFieldDate("updatedMillis", place.getUpdatedMillis());
        record.putField("ranking", place.getRanking());
        return record;
    }

    private static boolean equals(Place place, AirtableRecord record) {
        if (DateCompareUtils.after(record.getFieldDate("updatedMillis").getTime(), Duration.ofDays(2), place.getUpdatedMillis()))
            return false;

        if (!place.getStatus().getType().name().equals(record.getField("status").asText())) return false;
        if (!place.getName().equals(record.getField("name").asText())) return false;
        if (place.getTags().size() != record.getField("tags").size()) return false;

        if (!StringUtils.equals(place.getPhone(), record.getField("phone").asText())) return false;
        if (!StringUtils.equals(place.getWebsite(), record.getField("website").asText())) return false;
        if (!StringUtils.equals(place.getDescription(), record.getField("description").asText())) return false;

        if (!StringUtils.equals(place.getLocation().getPostcode(), record.getField("location.postcode").asText()))
            return false;
        return true;
    }
}
