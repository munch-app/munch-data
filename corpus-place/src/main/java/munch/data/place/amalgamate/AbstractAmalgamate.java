package munch.data.place.amalgamate;

import corpus.data.CatalystClient;
import corpus.data.CorpusClient;
import corpus.data.CorpusData;
import munch.data.place.elastic.ElasticPlace;
import munch.data.place.matcher.NameMatcher;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 8/12/2017
 * Time: 6:57 AM
 * Project: munch-data
 */
public abstract class AbstractAmalgamate {
    protected final Logger logger;

    protected CorpusClient corpusClient;
    protected CatalystClient catalystClient;

    protected NameMatcher nameMatcher;

    protected AbstractAmalgamate(Logger logger) {
        this.logger = logger;
    }

    @Inject
    void inject(CorpusClient corpusClient, CatalystClient catalystClient, NameMatcher nameMatcher) {
        this.corpusClient = corpusClient;
        this.catalystClient = catalystClient;
        this.nameMatcher = nameMatcher;
    }

    /**
     * @param placeData find more data to add
     */
    public void add(List<CorpusData> insides, CorpusData placeData) {
        // Existing insides
        long localCount = catalystClient.countCorpus(placeData.getCatalystId());

        search(placeData).forEachRemaining(result -> {
            CorpusData outside = corpusClient.get(result.getCorpusName(), result.getCorpusKey());
            if (!validate(placeData, insides, outside)) return;
            // If already inside, don't transfer either
            if (insides.contains(outside)) return;
            // If local count is smaller, don't transfer
            if (localCount < catalystClient.countCorpus(outside.getCatalystId())) return;

            // Move Data Over
            corpusClient.patchCatalystId(outside.getCorpusName(), outside.getCorpusKey(), placeData.getCatalystId());
            logger.info("Patched corpusName: {}, corpusKey: {} to catalystId: {}",
                    outside.getCorpusName(), outside.getCorpusKey(), placeData.getCatalystId());
        });
    }

    protected abstract Iterator<ElasticPlace> search(CorpusData placeData);

    protected abstract boolean validate(CorpusData placeData, List<CorpusData> insides, CorpusData outside);
}
