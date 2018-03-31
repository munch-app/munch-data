package munch.data.place.graph.seeder;

import corpus.data.CorpusClient;
import corpus.data.CorpusData;
import corpus.field.AbstractKey;
import corpus.field.PlaceKey;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 31/3/18
 * Time: 7:44 PM
 * Project: munch-data
 */
@Singleton
public class DecayTracker {
    public static final String CORPUS_NAME = "Sg.Munch.Place.Decaying";

    private final CorpusClient corpusClient;

    @Inject
    public DecayTracker(CorpusClient corpusClient) {
        this.corpusClient = corpusClient;
    }

    public void start(String placeId, String name, Duration duration) {
        CorpusData data = corpusClient.get(CORPUS_NAME, placeId);
        if (data != null) {
            // Decay speed override
            return;
        }

        data = new CorpusData();
        data.setCatalystId(placeId);
        data.put(PlaceKey.id, placeId);

        long startMillis = System.currentTimeMillis();
        data.put(DecayingKey.startMillis, startMillis);
        data.put(DecayingKey.endMillis, startMillis + duration.toMillis());
        data.put(DecayingKey.force, false);
        data.put(DecayingKey.name, name);

        corpusClient.put(CORPUS_NAME, placeId, data);
    }

    public void stop(String placeId) {
        corpusClient.delete(CORPUS_NAME, placeId);
    }

    @Nullable
    public Status find(List<CorpusData> dataList) {
        for (CorpusData data : dataList) {
            if (data.getCorpusName().equals(CORPUS_NAME)) {
                return new Status(data);
            }
        }

        return null;
    }

    public class Status {
        private final CorpusData corpusData;

        public Status(CorpusData corpusData) {
            this.corpusData = corpusData;
        }

        public Long getStartMillis() {
            return DecayingKey.startMillis.getValueLong(corpusData, 0L);
        }

        public Long getEndMillis() {
            return DecayingKey.endMillis.getValueLong(corpusData, 0L);
        }

        public Boolean isForce() {
            return DecayingKey.force.getValueBoolean(corpusData, false);
        }

        public String getDecayName() {
            return DecayingKey.name.getValue(corpusData);
        }

        public boolean isDecayed() {
            if (isForce()) return true;
            return getEndMillis() > System.currentTimeMillis();
        }
    }

    public static class DecayingKey extends AbstractKey {

        public static final DecayingKey startMillis = new DecayingKey("startMillis");
        public static final DecayingKey endMillis = new DecayingKey("endMillis");
        public static final DecayingKey force = new DecayingKey("force");
        public static final DecayingKey name = new DecayingKey("name");

        protected DecayingKey(String key) {
            super("Decaying." + key, false);
        }
    }
}
