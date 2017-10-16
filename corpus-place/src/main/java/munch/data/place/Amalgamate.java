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
            corpusClient.delete(placeData.getCorpusName(), placeData.getCorpusKey());
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

            // If treeList has only one item, most certainly it is the seeded item
            if (treeList.size() <= 1) return true;
            if (!placeMatcher.match(insides, outside)) {
                // Remove if don't match anymore
                listIterator.remove();
                logger.info("Removed corpusName: {}, corpusKey: {}, from catalystId: {}",
                        outside.getCorpusName(), outside.getCorpusKey(), outside.getCatalystId());
                corpusClient.patchCatalystId(outside.getCorpusName(), outside.getCorpusKey(), null);
            }
        }

        // If none left
        return !treeList.isEmpty();
    }

    /**
     * @param placeData find more data to add
     */
    private void add(CorpusData placeData) {
        // Existing insides
        List<CorpusData> insides = collectTree(placeData.getCatalystId());
        long localCount = catalystClient.countCorpus(placeData.getCatalystId());
        String postal = PlaceKey.Location.postal.get(placeData)
                .map(CorpusData.Field::getValue)
                .orElseThrow(NullPointerException::new);

        elasticClient.search(postal).forEachRemaining(result -> {
            CorpusData outside = corpusClient.get(result.getCorpusName(), result.getCorpusKey());
            if (outside == null) return;
            if (!placeMatcher.match(insides, outside)) return;
            if (localCount < catalystClient.countCorpus(outside.getCatalystId())) return;

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
}
