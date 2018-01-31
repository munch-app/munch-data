package munch.data.place.amalgamate;

import com.google.common.collect.ImmutableSet;
import com.typesafe.config.Config;
import corpus.data.CatalystClient;
import corpus.data.CorpusClient;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
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

    private final PostalAmalgamate postalAmalgamate;
    private final SpatialAmalgamate spatialAmalgamate;

    private final Set<String> names;
    private final Set<String> seedNames;

    @Inject
    public Amalgamate(Config config, CorpusClient corpusClient, CatalystClient catalystClient,
                      PostalAmalgamate postalAmalgamate, SpatialAmalgamate spatialAmalgamate) {
        this.corpusClient = corpusClient;
        this.catalystClient = catalystClient;

        this.postalAmalgamate = postalAmalgamate;
        this.spatialAmalgamate = spatialAmalgamate;

        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        builder.addAll(config.getStringList("place.postal"));
        builder.addAll(config.getStringList("place.spatial"));
        this.names = builder.build();
        this.seedNames = ImmutableSet.copyOf(config.getStringList("place.seed"));
    }

    /**
     * @param placeData Sg.Munch.Place
     * @return true if still exist, else deleted
     */
    public boolean maintain(CorpusData placeData) {
        if (validate(placeData)) {
            List<CorpusData> insides = collectTree(placeData.getCatalystId());
            postalAmalgamate.add(insides, placeData);
            spatialAmalgamate.add(insides, placeData);
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
            if (postalAmalgamate.validate(placeData, insides, outside)) continue;
            if (spatialAmalgamate.validate(placeData, insides, outside)) continue;

            // Remove if don't match anymore
            listIterator.remove();
            logger.info("Removed corpusName: {}, corpusKey: {}, from catalystId: {}",
                    outside.getCorpusName(), outside.getCorpusKey(), outside.getCatalystId());
            corpusClient.patchCatalystId(outside.getCorpusName(), outside.getCorpusKey(), null);
        }

        // No seeded data in the list
        return hasSeed(treeList);
    }

    /**
     * @param list corpus data list to check that has any seed
     * @return if any seed corpus data exists
     */
    private boolean hasSeed(List<CorpusData> list) {
        for (CorpusData data : list) {
            if (seedNames.contains(data.getCorpusName())) {
                return true;
            }
        }
        return false;
    }

    private List<CorpusData> collectTree(String catalystId) {
        List<CorpusData> treeList = new ArrayList<>();
        catalystClient.listCorpus(catalystId).forEachRemaining(data -> {
            if (names.contains(data.getCorpusName())) {
                treeList.add(data);
            }
        });
        return treeList;
    }

    /**
     * Overarching validator
     *
     * @param data data to check
     * @return whether data is valid
     */
    public static boolean isValid(CorpusData data) {
        if (data == null) return false;
        if (!PlaceKey.name.has(data)) return false;

        // Must have either postal or latLng
        if (PlaceKey.Location.postal.has(data)) return true;
        if (PlaceKey.Location.latLng.has(data)) return true;
        return false;
    }
}
