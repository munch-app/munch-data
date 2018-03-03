package munch.data.place;

import com.fasterxml.jackson.databind.ObjectMapper;
import corpus.airtable.AirtableApi;
import corpus.engine.CatalystEngine;
import munch.data.clients.PlaceClient;
import munch.data.structure.Place;
import munch.restful.core.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;

/**
 * Created by: Fuxing
 * Date: 14/2/18
 * Time: 7:35 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceAwardCorpus extends CatalystEngine<Place> {
    private static final Logger logger = LoggerFactory.getLogger(PlaceAwardCorpus.class);


    private final ObjectMapper mapper = JsonUtils.objectMapper;
    private final PlaceClient placeClient;
    private final AirtableApi.Table airtable;

    @Inject
    public PlaceAwardCorpus(PlaceClient placeClient, AirtableApi airtableApi) {
        super(logger);
        this.placeClient = placeClient;
        this.airtable = airtableApi.base("apphY7zE8Tdd525qO").table("New Place");
    }

    @Override
    protected Duration cycleDelay() {
        // Sync every 12 hours
        return Duration.ofHours(12);
    }

    @Override
    protected Iterator<Place> fetch(long cycleNo) {
        return null;
    }

    @Override
    protected void doCycle(long cycleNo, Iterator<Place> iterator) {

    }

    @Override
    protected void process(long cycleNo, Place place, long processed) {
    }
}
