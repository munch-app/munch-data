package munch.data.place.amalgamate;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.place.elastic.ElasticPlace;
import munch.data.place.elastic.PostalClient;
import munch.data.place.matcher.PostalMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 8/12/2017
 * Time: 6:56 AM
 * Project: munch-data
 */
@Singleton
public final class PostalAmalgamate extends AbstractAmalgamate {
    private static final Logger logger = LoggerFactory.getLogger(PostalAmalgamate.class);

    private final PostalClient postalClient;
    private final PostalMatcher postalMatcher;

    @Inject
    public PostalAmalgamate(PostalClient postalClient, PostalMatcher postalMatcher) {
        super(logger);
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
    protected boolean validate(CorpusData placeData, List<CorpusData> insides, CorpusData outside) {
        if (!Amalgamate.isValid(outside)) return false;
        if (insides.isEmpty()) return true;

        if (!postalMatcher.match(placeData, insides, outside)) return false;
        if (!nameMatcher.match(placeData, insides, outside)) return false;
        return true;
    }
}
