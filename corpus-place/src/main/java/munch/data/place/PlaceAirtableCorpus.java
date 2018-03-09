package munch.data.place;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableRecord;
import corpus.airtable.AirtableReplaceSession;
import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import munch.data.clients.PlaceClient;
import munch.data.structure.Place;
import munch.restful.core.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 9/3/18
 * Time: 6:42 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceAirtableCorpus extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(PlaceAirtableCorpus.class);
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // 2017-10-16T00:00:00.000Z

    private final PlaceClient placeClient;
    private final AirtableApi.Table table;
    private final Set<String> possiblePlaceTags;

    private AirtableReplaceSession replaceSession;

    @Inject
    public PlaceAirtableCorpus(PlaceClient placeClient, AirtableApi airtableApi) throws IOException {
        super(logger);
        this.placeClient = placeClient;
        this.table = airtableApi.base("appJ5aqNU0ergMEf7").table("Place");

        URL url = Resources.getResource("airtable-place-tags.txt");
        this.possiblePlaceTags = ImmutableSet.copyOf(Resources.readLines(url, Charset.forName("UTF-8")));
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(48);
    }

    @Override
    protected boolean preCycle(long cycleNo) {
        this.replaceSession = new AirtableReplaceSession(Duration.ofSeconds(2), table, (record, record2) -> {
            return record.getField("Place.id").asText().equals(record2.getField("Place.id").asText());
        }, (record, record2) -> {
            return record.getField("UpdatedDate").asText().equals(record2.getField("UpdatedDate").asText());
        });
        return super.preCycle(cycleNo);
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return corpusClient.list("Sg.Munch.Place");
    }

    @Override
    protected void process(long cycleNo, CorpusData data, long processed) {
        String placeId = data.getCatalystId();

        Place place = placeClient.get(placeId);
        if (place == null) return;

        AirtableRecord record = parse(place);
        replaceSession.put(record);
    }

    private AirtableRecord parse(Place place) {
        AirtableRecord record = new AirtableRecord();
        Map<String, JsonNode> fields = new HashMap<>();
        fields.put("Place.id", JsonUtils.toTree(place.getId()));
        fields.put("Place.status", JsonUtils.toTree(place.isOpen() ? "Open" : "Close"));

        fields.put("Place.name", JsonUtils.toTree(place.getName()));
        fields.put("Place.allNames", JsonUtils.toTree(Joiner.on("\n").join(place.getAllNames())));
        fields.put("Place.description", JsonUtils.toTree(place.getDescription()));
        fields.put("Place.phone", JsonUtils.toTree(place.getPhone()));

        fields.put("Place.menuUrl", JsonUtils.toTree(place.getMenuUrl()));
        fields.put("Place.website", JsonUtils.toTree(place.getWebsite()));

        if (place.getPrice() != null) {
            fields.put("Place.price", JsonUtils.toTree(place.getPrice().getMiddle()));
        }

        fields.put("Place.tags", JsonUtils.toTree(getTags(place)));

        if (place.getReview() != null) {
            fields.put("Place.Review.average", JsonUtils.toTree(place.getReview().getAverage()));
            fields.put("Place.Review.total", JsonUtils.toTree(place.getReview().getTotal()));
        }

        fields.put("Count.hours", JsonUtils.toTree(place.getHours().size()));
        fields.put("Count.images", JsonUtils.toTree(place.getImages().size()));
        fields.put("Count.containers", JsonUtils.toTree(place.getContainers().size()));

        fields.put("Place.Location.address", JsonUtils.toTree(place.getLocation().getAddress()));
        fields.put("Place.Location.postal", JsonUtils.toTree(place.getLocation().getPostal()));
        fields.put("Place.Location.neighbourhood", JsonUtils.toTree(place.getLocation().getNeighbourhood()));
        fields.put("Place.Location.street", JsonUtils.toTree(place.getLocation().getStreet()));
        fields.put("Place.Location.unitNumber", JsonUtils.toTree(place.getLocation().getUnitNumber()));
        fields.put("Place.Location.latLng", JsonUtils.toTree(place.getLocation().getLatLng()));

        fields.put("ranking", JsonUtils.toTree(place.getRanking()));
        fields.put("CreatedDate", JsonUtils.toTree(DATE_FORMAT.format(place.getCreatedDate())));
        fields.put("UpdatedDate", JsonUtils.toTree(DATE_FORMAT.format(place.getUpdatedDate())));
        record.setFields(fields);
        return record;
    }

    private List<String> getTags(Place place) {
        if (place.getTag() == null) return List.of();
        return place.getTag().getExplicits().stream()
                .filter(s -> {
                    if (possiblePlaceTags.contains(s.toLowerCase())) return true;
                    logger.warn("Place Tag {} Not Found in Possible List", s);
                    return false;
                })
                .collect(Collectors.toList());
    }

    @Override
    protected void postCycle(long cycleNo) {
        replaceSession.close();
        replaceSession = null;
        super.postCycle(cycleNo);
    }
}
