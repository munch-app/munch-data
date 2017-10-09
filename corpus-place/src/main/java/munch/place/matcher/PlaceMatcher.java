package munch.place.matcher;

import corpus.data.CorpusData;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created By: Fuxing Loh
 * Date: 8/10/2017
 * Time: 8:38 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceMatcher {

    private final LocationMatcher locationMatcher;
    private final NameMatcher nameMatcher;

    @Inject
    public PlaceMatcher(LocationMatcher locationMatcher, NameMatcher nameMatcher) {
        this.locationMatcher = locationMatcher;
        this.nameMatcher = nameMatcher;
    }

    /**
     * @param insides data already inside
     * @param outside data outside coming in
     * @return true is outside data belongs with inside
     */
    public boolean match(List<CorpusData> insides, CorpusData outside) {
        if (!locationMatcher.match(insides, outside)) return false;
        if (!nameMatcher.match(insides, outside)) return false;
        return true;
    }
}
