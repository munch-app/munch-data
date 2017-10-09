package munch.data.tag;

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
public class TagKey extends AbstractKey {

    public static final TagKey updatedDate = new TagKey("updatedDate", false);

    private TagKey(String key, boolean multi) {
        super("Sg.Munch.Tag." + key, multi);
    }

    public boolean equal(CorpusData data, Date date) {
        String right = Long.toString(date.getTime());
        return StringUtils.equals(getValue(data), right);
    }
}
