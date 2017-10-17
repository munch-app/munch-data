package munch.data.place;

import corpus.data.CatalystClient;
import corpus.data.CorpusClient;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.place.elastic.ElasticClient;
import munch.data.place.matcher.PlaceMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 9/10/2017
 * Time: 6:19 PM
 * Project: munch-data
 */
@Singleton
public final class Amalgamate {
    private static final Logger logger = LoggerFactory.getLogger(Amalgamate.class);

    private final CorpusClient corpusClient;
    private final CatalystClient catalystClient;
    private final ElasticClient elasticClient;

    private final Set<String> treeNames;
    private final PlaceMatcher placeMatcher;

    @Inject
    public Amalgamate(CorpusClient corpusClient, CatalystClient catalystClient, ElasticClient elasticClient,
                      @Named("place.trees") Set<String> treeNames, PlaceMatcher placeMatcher) {
        this.corpusClient = corpusClient;
        this.catalystClient = catalystClient;
        this.elasticClient = elasticClient;
        this.treeNames = treeNames;
        this.placeMatcher = placeMatcher;
    }

    /**
     * @param placeData Sg.Munch.Place
     * @return true if still exist, else deleted
     */
    public boolean maintain(CorpusData placeData) {
        if (validate(placeData)) {
            add(placeData);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param placeData Sg.Munch.Place
     * @return true if still exist
     */
    private boolean validate(CorpusData placeData) {
        List<CorpusData> treeList = collectTree(placeData.getCatalystId());
        if (treeList.isEmpty()) return false;

        ListIterator<CorpusData> listIterator = treeList.listIterator();
        while (listIterator.hasNext()) {
            CorpusData outside = listIterator.next();
            List<CorpusData> insides = new ArrayList<>(treeList);
            insides.remove(outside);

            // If successfully validated, can skip it
            if (validate(insides, outside)) continue;

            // Remove if don't match anymore
            listIterator.remove();
            logger.info("Removed corpusName: {}, corpusKey: {}, from catalystId: {}",
                    outside.getCorpusName(), outside.getCorpusKey(), outside.getCatalystId());
            corpusClient.patchCatalystId(outside.getCorpusName(), outside.getCorpusKey(), null);
        }

        // If none left
        return treeList.size() > 0;
    }

    /**
     * @param insides insides
     * @param outside outside, exiting
     * @return true = can stay inside, false = must exit
     */
    private boolean validate(List<CorpusData> insides, CorpusData outside) {
        if (!isValid(outside)) return false;
        if (insides.isEmpty()) return true;
        if (!placeMatcher.match(insides, outside)) return false;
        return true;
    }

    /**
     * @param placeData find more data to add
     */
    private void add(CorpusData placeData) {
        // Existing insides
        List<CorpusData> insides = collectTree(placeData.getCatalystId());
        long localCount = catalystClient.countCorpus(placeData.getCatalystId());
        String postal = PlaceKey.Location.postal.getValueOrThrow(placeData);

        elasticClient.search(postal).forEachRemaining(result -> {
            CorpusData outside = corpusClient.get(result.getCorpusName(), result.getCorpusKey());
            if (!validate(insides, outside)) return;
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

    private List<CorpusData> collectTree(String catalystId) {
        List<CorpusData> treeList = new ArrayList<>();
        catalystClient.listCorpus(catalystId).forEachRemaining(data -> {
            if (treeNames.contains(data.getCorpusName())) {
                treeList.add(data);
            }
        });
        return treeList;
    }

    /**
     * @param data data to check
     * @return whether data is valid
     */
    public boolean isValid(CorpusData data) {
        if (data == null) return false;
        if (!PlaceKey.name.has(data)) return false;
        if (!PlaceKey.Location.postal.has(data)) return false;
        return true;
    }
}
