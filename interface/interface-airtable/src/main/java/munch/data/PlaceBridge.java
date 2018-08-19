package munch.data;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterators;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableRecord;
import corpus.engine.AbstractEngine;
import munch.data.client.ElasticClient;
import munch.data.client.PlaceClient;
import munch.data.elastic.ElasticUtils;
import munch.data.place.AirtablePlaceMapper;
import munch.data.place.Place;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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
    private final ElasticClient elasticClient;

    private final AirtableApi.Table placeTable;
    private final AirtablePlaceMapper placeMapper;

    @Inject
    public PlaceBridge(AirtableApi api, PlaceClient placeClient, ElasticClient elasticClient, AirtablePlaceMapper placeMapper) {
        super(logger);
        this.placeClient = placeClient;
        this.placeTable = api.base("appDcx5b3vgkhcYB5").table("Place");
        this.elasticClient = elasticClient;
        this.placeMapper = placeMapper;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(8);
    }

    @Override
    protected Iterator<Object> fetch(long cycleNo) {
        ArrayNode sort = JsonUtils.createArrayNode();
        sort.addObject().put("placeId", "desc");
        return Iterators.concat(searchBubbleTea().iterator(), placeTable.select(sort), placeClient.iterator());
    }

    private List<Place> searchBubbleTea() {
        ObjectNode root = JsonUtils.createObjectNode();
        root.put("from", 0);
        root.put("size", 500);

        ObjectNode bool = JsonUtils.createObjectNode();
        bool.set("must", ElasticUtils.mustMatchAll());
        bool.set("filter", JsonUtils.createArrayNode()
                .add(ElasticUtils.filterTerm("dataType", "Place"))
                .add(ElasticUtils.filterTerm("tags.name", "Bubble Tea".toLowerCase()))
        );
        root.putObject("query").set("bool", bool);

        List<Place> places = elasticClient.searchHitsHits(root);
        logger.info("Searched BubbleTea: {}", places.size());
        return places;
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
        sleep(125);

        if (records.size() == 0) {
            // Posted
            placeTable.post(placeMapper.parse(place));
            sleep(125);
            return;
        }
        if (records.size() == 1) {
            // Patched
            AirtableRecord record = records.get(0);
            if (equals(place, record)) return;
            AirtableRecord patchRecord = placeMapper.parse(place);
            patchRecord.setId(record.getId());
            placeTable.patch(patchRecord);
            sleep(125);
            return;
        }

        throw new IllegalStateException("More then 1 Place with the same id.");
    }


    private static boolean equals(Place place, AirtableRecord record) {
        if (!place.getStatus().getType().name().equals(record.getField("status").asText())) return false;
        if (!place.getName().equals(record.getField("name").asText())) return false;
        if (place.getTags().size() != record.getField("tags").size()) return false;

        if (!StringUtils.equals(place.getPhone(), record.getField("phone").asText())) return false;
        if (!StringUtils.equals(place.getWebsite(), record.getField("website").asText())) return false;
        if (!StringUtils.equals(place.getDescription(), record.getField("description").asText())) return false;

        if (!StringUtils.equals(place.getLocation().getPostcode(), record.getField("location.postcode").asText()))
            return false;

        return Objects.requireNonNull(record.getFieldDate("updatedMillis")).getTime() == place.getUpdatedMillis();
    }
}
