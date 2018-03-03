package munch.data.place;

import com.fasterxml.jackson.databind.ObjectMapper;
import corpus.airtable.AirtableApi;
import corpus.engine.CatalystEngine;
import munch.data.clients.PlaceClient;
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
public final class PlaceAwardCorpus extends CatalystEngine<AwardCollection> {
    private static final Logger logger = LoggerFactory.getLogger(PlaceAwardCorpus.class);


    private final ObjectMapper mapper = JsonUtils.objectMapper;
    private final PlaceClient placeClient;
    private final AirtableApi.Table airtable;
    private final AirtableDatabase airtableDatabase;

    @Inject
    public PlaceAwardCorpus(PlaceClient placeClient, AirtableApi airtableApi, AirtableDatabase airtableDatabase) {
        super(logger);
        this.placeClient = placeClient;
        this.airtable = airtableApi.base("apphY7zE8Tdd525qO").table("New Place");
        this.airtableDatabase = airtableDatabase;
    }

    @Override
    protected Duration cycleDelay() {
        // Sync every 12 hours
        return Duration.ofHours(12);
    }

    @Override
    protected Iterator<AwardCollection> fetch(long cycleNo) {
        return airtableDatabase.list();
    }

    @Override
    protected void process(long cycleNo, AwardCollection awardCollection, long processed) {
        awardCollection.getAwardPlaces().forEach(awardPlace -> {
            awardPlace.tryLink((name, address) -> {
                // Sleep Here
                // Search and try to link all the places
                return null;
            });
        });

        // TODO
        // Search if collection already exists, else create
        // Collection must have unique Id to track unique

        // Get Collection List:
        // 1. delete those that should not be in the list
        // 2. add those that are not in the list

        // TODO Put Award Into Database, PlaceCard
    }
}
