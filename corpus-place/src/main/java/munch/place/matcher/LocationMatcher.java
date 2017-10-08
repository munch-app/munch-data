package munch.place.matcher;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;
import java.util.List;

/**
 * Created By: Fuxing Loh
 * Date: 8/10/2017
 * Time: 8:44 PM
 * Project: munch-data
 */
@Singleton
public final class LocationMatcher {

    /**
     * @param insides data already inside
     * @param outside data outside coming in
     * @return true is outside data belongs with inside
     */
    public boolean match(List<CorpusData> insides, CorpusData outside) {
        return matchPostal(insides, outside);
    }

    public boolean matchPostal(List<CorpusData> insides, CorpusData outside) {
        String postal = PlaceKey.Location.postal.getValue(outside);
        if (postal == null) return false;

        for (CorpusData data : insides) {
            for (CorpusData.Field field : PlaceKey.Location.postal.getAll(data)) {
                if (matchPostal(field.getValue(), postal)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean matchPostal(String left, String right) {
        return StringUtils.equals(fixPostal(left), fixPostal(right));
    }

    private String fixPostal(String postal) {
        if (postal != null && postal.length() == 5) return "0" + postal;
        return postal;
    }
}
