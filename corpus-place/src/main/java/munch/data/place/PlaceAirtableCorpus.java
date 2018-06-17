package munch.data.place;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableHashSession;
import corpus.airtable.AirtableRecord;
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
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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

    private final PlaceClient placeClient;
    private final AirtableApi.Table table;
    private final Set<String> possiblePlaceTags;

    private AirtableHashSession hashSession;

    @Inject
    public PlaceAirtableCorpus(PlaceClient placeClient, AirtableApi airtableApi) throws IOException {
        super(logger);
        this.placeClient = placeClient;
        this.table = airtableApi.base("appIpnSUoqQqpOwF3").table("Place");

        URL url = Resources.getResource("airtable-place-tags.txt");
        this.possiblePlaceTags = ImmutableSet.copyOf(Resources.readLines(url, Charset.forName("UTF-8")));
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(48);
    }

    @Override
    protected boolean preCycle(long cycleNo) {
        this.hashSession = new AirtableHashSession(table, Duration.ofMillis(500), "Place.id", "UpdatedDate");
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
        if (place == null) {
            sleep(200);
            return;
        }

        AirtableRecord record = parse(place);
        hashSession.put(record);
    }

    private AirtableRecord parse(Place place) {
        AirtableRecord record = new AirtableRecord();
        record.putField("Place.id", place.getId());
        record.putField("Place.status", JsonUtils.toTree(place.isOpen() ? "Open" : "Close"));

        record.putField("Place.name", place.getName());
        record.putField("Place.allNames", Joiner.on("\n").join(place.getAllNames()));
        record.putField("Place.description", place.getDescription());
        record.putField("Place.phone", place.getPhone());

        record.putField("Place.menuUrl", place.getMenuUrl());
        record.putField("Place.website", place.getWebsite());

        if (place.getPrice() != null) {
            record.putField("Place.price", JsonUtils.toTree(place.getPrice().getMiddle()));
        }

        record.putField("Place.tags", JsonUtils.toTree(getTags(place)));

        if (place.getReview() != null) {
            record.putField("Place.Review.average", JsonUtils.toTree(place.getReview().getAverage() * 100));
            record.putField("Place.Review.total", JsonUtils.toTree(place.getReview().getTotal()));
        }

        record.putField("Contains.containers", JsonUtils.toTree(place.getContainers().size() > 0));
        record.putField("Contains.hours", JsonUtils.toTree(place.getHours().size() > 0));
        record.putField("Contains.images", JsonUtils.toTree(place.getImages().size() > 0));
        record.putField("Count.images", JsonUtils.toTree(place.getImages().size()));

        record.putField("Place.Location.address", JsonUtils.toTree(place.getLocation().getAddress()));
        record.putField("Place.Location.postal", JsonUtils.toTree(place.getLocation().getPostal()));
        record.putField("Place.Location.neighbourhood", JsonUtils.toTree(place.getLocation().getNeighbourhood()));
        record.putField("Place.Location.street", JsonUtils.toTree(place.getLocation().getStreet()));
        record.putField("Place.Location.unitNumber", JsonUtils.toTree(place.getLocation().getUnitNumber()));
        record.putField("Place.Location.latLng", JsonUtils.toTree(place.getLocation().getLatLng()));

        record.putField("ranking", JsonUtils.toTree(place.getRanking()));
        record.putFieldDate("CreatedDate", place.getCreatedDate());
        record.putFieldDate("UpdatedDate", place.getUpdatedDate());
        return record;
    }

    private List<String> getTags(Place place) {
        if (place.getTag() == null) return List.of();
        return place.getTag().getImplicits().stream()
                .filter(s -> {
                    if (possiblePlaceTags.contains(s.toLowerCase())) return true;
                    logger.warn("Place Tag {} Not Found in Possible List", s);
                    return false;
                })
                .collect(Collectors.toList());
    }

    @Override
    protected void postCycle(long cycleNo) {
        hashSession.close();
        hashSession = null;
        super.postCycle(cycleNo);
    }
}
