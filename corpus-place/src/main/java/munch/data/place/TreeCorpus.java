package munch.data.place;

import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.exception.NotFoundException;
import corpus.field.PlaceKey;
import munch.data.clients.PlaceClient;
import munch.data.structure.Place;
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
public class TreeCorpus extends CatalystEngine<CorpusData> {
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
        return Duration.ofMinutes(15);
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
                put(placeData, placeParser.parse(list));
            } else {
                placeClient.delete(placeData.getCorpusKey());
            }

            // Sleep for 1 second every 5 processed
            if (processed % 5 == 0) {
                sleep(1000);
            }
        } catch (NotFoundException e) {
            logger.warn("Amalgamate Conflict Error catalystId: {}", placeData.getCatalystId(), e);
        }
    }

    /**
     * Data put only if actually changed
     *
     * @param placeData place data of the actual card
     * @param place     non null place
     */
    public void put(CorpusData placeData, Place place) {
        String placeId = placeData.getCatalystId();
        Objects.requireNonNull(placeId);
        Place existing = placeClient.get(placeId);

        // Put if data is changed only
        if (place.equals(existing)) return;

        // Put to corpus client
        // CACHED FEEDBACK LOOP, Parser will read from here also
        placeData.put(PlaceKey.name, place.getName());
        placeData.put(PlaceKey.Location.postal, place.getLocation().getPostal());
        placeData.put(PlaceKey.Location.latLng, place.getLocation().getLatLng());
        corpusClient.put(corpusName, placeData.getCorpusKey(), placeData);

        // Put to place client
        placeClient.put(place);
    }
}
