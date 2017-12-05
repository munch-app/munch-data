package munch.data.place;

import com.google.common.collect.ImmutableSet;
import com.typesafe.config.Config;
import corpus.data.CatalystClient;
import corpus.data.CorpusClient;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.place.elastic.ElasticPlace;
import munch.data.place.elastic.PostalClient;
import munch.data.place.elastic.SpatialClient;
import munch.data.place.matcher.NameMatcher;
import munch.data.place.matcher.PostalMatcher;
import munch.data.place.matcher.SpatialMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

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

    private final NameMatcher nameMatcher;
    private final PostalAmalgamate postalAmalgamate;
    private final SpatialAmalgamate spatialAmalgamate;

    private final Set<String> names;

    @Inject
    public Amalgamate(Config config, CorpusClient corpusClient, CatalystClient catalystClient,
                      NameMatcher nameMatcher,
                      PostalClient postalClient, PostalMatcher postalMatcher,
                      SpatialClient spatialClient, SpatialMatcher spatialMatcher) {
        this.corpusClient = corpusClient;
        this.catalystClient = catalystClient;

        this.nameMatcher = nameMatcher;
        this.postalAmalgamate = new PostalAmalgamate(postalClient, postalMatcher);
        this.spatialAmalgamate = new SpatialAmalgamate(spatialClient, spatialMatcher);

        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        builder.addAll(config.getStringList("place.postal"));
        builder.addAll(config.getStringList("place.spatial"));
        this.names = builder.build();
    }

    /**
     * @param placeData Sg.Munch.Place
     * @return true if still exist, else deleted
     */
    public boolean maintain(CorpusData placeData) {
        if (validate(placeData)) {
            postalAmalgamate.add(placeData);
            spatialAmalgamate.add(placeData);
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
            if (postalAmalgamate.validate(insides, outside)) continue;
            if (spatialAmalgamate.validate(insides, outside)) continue;

            // Remove if don't match anymore
            listIterator.remove();
            logger.info("Removed corpusName: {}, corpusKey: {}, from catalystId: {}",
                    outside.getCorpusName(), outside.getCorpusKey(), outside.getCatalystId());
            corpusClient.patchCatalystId(outside.getCorpusName(), outside.getCorpusKey(), null);
        }

        // If none left
        return treeList.size() > 0;
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

    public class PostalAmalgamate extends AbstractAmalgamate {
        private final PostalClient postalClient;
        private final PostalMatcher postalMatcher;

        public PostalAmalgamate(PostalClient postalClient, PostalMatcher postalMatcher) {
            super(LoggerFactory.getLogger(PostalAmalgamate.class));
            this.postalClient = postalClient;
            this.postalMatcher = postalMatcher;
        }

        @Override
        protected Iterator<ElasticPlace> search(CorpusData placeData) {
            String postal = PlaceKey.Location.postal.getValueOrThrow(placeData);
            return postalClient.search(postal);
        }

        /**
         * @param insides insides
         * @param outside outside, exiting
         * @return true = can stay inside, false = must exit
         */
        @Override
        @SuppressWarnings("Duplicates")
        protected boolean validate(List<CorpusData> insides, CorpusData outside) {
            if (!isValid(outside)) return false;
            if (insides.isEmpty()) return true;

            if (!postalMatcher.match(insides, outside)) return false;
            if (!nameMatcher.match(insides, outside)) return false;
            return true;
        }
    }

    public class SpatialAmalgamate extends AbstractAmalgamate{
        private final SpatialClient spatialClient;
        private final SpatialMatcher spatialMatcher;

        public SpatialAmalgamate(SpatialClient spatialClient, SpatialMatcher spatialMatcher) {
            super(LoggerFactory.getLogger(SpatialAmalgamate.class));
            this.spatialClient = spatialClient;
            this.spatialMatcher = spatialMatcher;
        }

        @Override
        protected Iterator<ElasticPlace> search(CorpusData placeData) {
            String latLng = PlaceKey.Location.latLng.getValueOrThrow(placeData);
            return spatialClient.search(latLng, SpatialMatcher.MAX_DISTANCE);
        }

        /**
         * @param insides insides
         * @param outside outside, exiting
         * @return true = can stay inside, false = must exit
         */
        @Override
        @SuppressWarnings("Duplicates")
        protected boolean validate(List<CorpusData> insides, CorpusData outside) {
            if (!isValid(outside)) return false;
            if (insides.isEmpty()) return true;

            if (!spatialMatcher.match(insides, outside)) return false;
            if (!nameMatcher.match(insides, outside)) return false;
            return true;
        }
    }

    public abstract class AbstractAmalgamate {
        protected final Logger logger;

        protected AbstractAmalgamate(Logger logger) {
            this.logger = logger;
        }

        /**
         * @param placeData find more data to add
         */
        public void add(CorpusData placeData) {
            // Existing insides
            List<CorpusData> insides = collectTree(placeData.getCatalystId());
            long localCount = catalystClient.countCorpus(placeData.getCatalystId());

            search(placeData).forEachRemaining(result -> {
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

        protected abstract Iterator<ElasticPlace> search(CorpusData placeData);

        protected abstract boolean validate(List<CorpusData> insides, CorpusData outside);
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
