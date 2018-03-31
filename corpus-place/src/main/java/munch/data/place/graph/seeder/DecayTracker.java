package munch.data.place.graph.seeder;

import corpus.data.CorpusClient;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 31/3/18
 * Time: 7:44 PM
 * Project: munch-data
 */
@Singleton
public class DecayTracker {
    private final CorpusClient corpusClient;

    @Inject
    public DecayTracker(CorpusClient corpusClient) {
        this.corpusClient = corpusClient;
    }

    public void start(String placeId, String name, Duration duration) {
        CorpusData data = corpusClient.get("Sg.Munch.Place.Decaying", placeId);
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
        data.put(DecayingKey.name, name);
        data.put(PlaceKey.id, placeId);

        corpusClient.put("Sg.Munch.Place.Decaying", placeId, data);
    }

    public void stop(String placeId, Status status) {
        if (status != null) {
            corpusClient.delete("Sg.Munch.Place.Decaying", placeId);
        }
    }

    @Nullable
    public Status find(List<CorpusData> dataList) {
        Set<String> corpusNames = dataList.stream()
                .map(CorpusData::getCorpusName)
                .collect(Collectors.toSet());

        if (corpusNames.contains("Sg.Munch.Place.Decaying")) return new Status(corpusNames);
        if (corpusNames.contains("Sg.Munch.Place.Decaying.Decayed")) return new Status(corpusNames);
        return null;
    }

    public class Status {
        private final Set<String> corpusNames;

        public Status(Set<String> corpusNames) {
            this.corpusNames = corpusNames;
        }

        public boolean isDecayed() {
            return corpusNames.contains("Sg.Munch.Place.Decaying.Decayed");
        }
    }
}
