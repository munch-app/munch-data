package munch.data.place.graph.seeder;

import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.field.PlaceKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;

/**
 * Created by: Fuxing
 * Date: 1/4/2018
 * Time: 2:20 AM
 * Project: munch-data
 */
@Singleton
public final class DecayCorpus extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(DecayCorpus.class);

    @Inject
    public DecayCorpus() {
        super(logger);
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(2);
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return corpusClient.list("Sg.Munch.Place.Decaying");
    }

    @Override
    protected void process(long cycleNo, CorpusData data, long processed) {

    }

    // TODO
    // From Airtable to (Resolve if Any, Remove Expired)

    // List all Decaying
    // Resolve Expired
    // Update Everything Else

    public void resolve(String placeId, boolean decayed) {
        CorpusData data = new CorpusData();
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
