package munch.data.location;

import corpus.data.CorpusData;
import corpus.field.AbstractKey;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Created by: Fuxing
 * Date: 10/10/2017
 * Time: 3:48 AM
 * Project: munch-data
 */
public class LocationKey extends AbstractKey {

    public static final LocationKey updatedDate = new LocationKey("updatedDate", false);

    private LocationKey(String key, boolean multi) {
        super("Sg.Munch.Location." + key, multi);
    }

    public boolean equal(CorpusData.Field field, Date date) {
        String right = Long.toString(date.getTime());
        return StringUtils.equals(field.getValue(), right);
    }

    public CorpusData.Field getOrThrow(CorpusData data) {
        return get(data).orElseThrow(NullPointerException::new);
    }
}
