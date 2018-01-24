package munch.data.place.amalgamate;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.place.elastic.ElasticPlace;
import munch.data.place.elastic.SpatialClient;
import munch.data.place.matcher.SpatialMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 8/12/2017
 * Time: 6:57 AM
 * Project: munch-data
 */
@Singleton
public final class SpatialAmalgamate extends AbstractAmalgamate {
    private static final Logger logger = LoggerFactory.getLogger(SpatialAmalgamate.class);

    private final SpatialClient spatialClient;
    private final SpatialMatcher spatialMatcher;

    @Inject
    public SpatialAmalgamate(SpatialClient spatialClient, SpatialMatcher spatialMatcher) {
        super(logger);
        this.spatialClient = spatialClient;
        this.spatialMatcher = spatialMatcher;
    }

    @Override
    protected Iterator<ElasticPlace> search(CorpusData placeData) {
        // Some Sg.Munch.Place might not have latLng ready for use
        return PlaceKey.Location.latLng.get(placeData)
                .map(CorpusData.Field::getValue)
                .map(latLng -> spatialClient.search(latLng, SpatialMatcher.MAX_DISTANCE))
                .orElse(Collections.emptyIterator());
    }

    /**
     * @param insides insides
     * @param outside outside, exiting
     * @return true = can stay inside, false = must exit
     */
    @Override
    @SuppressWarnings("Duplicates")
    protected boolean validate(CorpusData placeData, List<CorpusData> insides, CorpusData outside) {
        if (!Amalgamate.isValid(outside)) return false;
        if (insides.isEmpty()) return true;

        if (!spatialMatcher.match(placeData, insides, outside)) return false;
        if (!nameMatcher.match(placeData, insides, outside)) return false;
        return true;
    }
}
