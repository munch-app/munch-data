package munch.data.place;

import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.exception.NotFoundException;
import corpus.field.PlaceKey;
import munch.data.clients.PlaceClient;
import munch.data.structure.Place;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Created By: Fuxing Loh
 * Date: 8/10/2017
 * Time: 7:39 PM
 * Project: munch-data
 */
@Singleton
public final class TreeCorpus extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(TreeCorpus.class);

    private final Amalgamate amalgamate;
    private final PlaceClient placeClient;
    private final PlaceParser placeParser;

    @Inject
    public TreeCorpus(Amalgamate amalgamate, PlaceClient placeClient, PlaceParser placeParser) {
        super(logger);
        this.amalgamate = amalgamate;
        this.placeClient = placeClient;
        this.placeParser = placeParser;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofMinutes(20);
    }

    @Override
    protected long loadCycleNo() {
        return System.currentTimeMillis();
    }

    /**
     * @param cycleNo current cycleNo
     * @return all data to maintain
     */
    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return corpusClient.list(corpusName);
    }

    /**
     * Maintain Each(Place)
     * 1. Validate()
     * 2. Add()
     *
     * @param cycleNo   cycleNo current cycleNo
     * @param placeData each data to process
     * @param processed processed data count
     */
    @Override
    protected void process(long cycleNo, CorpusData placeData, long processed) {
        try {
            if (amalgamate.maintain(placeData)) {
                List<CorpusData> list = new ArrayList<>();
                catalystClient.listCorpus(placeData.getCatalystId()).forEachRemaining(list::add);

                Place place = placeParser.parse(new Place(), list);
                // Null = parsing failed
                if (place == null) {
                    deleteIf(placeData.getCorpusKey());
                } else {
                    putIf(place);
                    putPlaceData(placeData, place);
                    count(place);
                }
            } else {
                deleteIf(placeData.getCorpusKey());
                corpusClient.delete(placeData.getCorpusName(), placeData.getCorpusKey());
            }

            // Sleep for 0.3 second every 4 processed
            if (processed % 4 == 0) sleep(300);
            if (processed % 1000 == 0) logger.info("Processed {} places", processed);
        } catch (NotFoundException e) {
            logger.warn("Amalgamate Conflict Error catalystId: {}", placeData.getCatalystId(), e);
        }
    }

    private void count(Place place) {
        counter.increment("Counts.Places");

        if (!place.getImages().isEmpty()) counter.increment("Counts.Images");
        if (!place.getHours().isEmpty()) counter.increment("Counts.Hours");
        if (StringUtils.isNotBlank(place.getPhone())) counter.increment("Counts.Phone");
        if (StringUtils.isNotBlank(place.getWebsite())) counter.increment("Counts.Website");
    }

    /**
     * Data put only if actually changed
     *
     * @param place non null place
     */
    private void putIf(Place place) {
        Objects.requireNonNull(place.getId());

        // Put if data is changed only
        Place existing = placeClient.get(place.getId());
        if (!place.equals(existing)) {
            placeClient.put(place);
            counter.increment("Updated");
        }
    }

    private void deleteIf(String placeId) {
        Objects.requireNonNull(placeId);

        // Delete if exist only
        Place existing = placeClient.get(placeId);
        if (existing != null) {
            placeClient.delete(placeId);
        }
    }

    private void putPlaceData(CorpusData placeData, Place place) {
        // Put to corpus client
        // CACHED FEEDBACK LOOP, Parser will read from here also
        placeData.replace(PlaceKey.name, place.getName());
        placeData.replace(PlaceKey.Location.postal, place.getLocation().getPostal());
        placeData.replace(PlaceKey.Location.latLng, place.getLocation().getLatLng());
        corpusClient.put(corpusName, placeData.getCorpusKey(), placeData);
    }
}
