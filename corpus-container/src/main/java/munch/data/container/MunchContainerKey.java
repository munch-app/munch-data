package munch.data.container;

import corpus.data.CorpusData;
import corpus.field.AbstractKey;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Created by: Fuxing
 * Date: 10/12/2017
 * Time: 11:27 AM
 * Project: munch-data
 */
public final class MunchContainerKey extends AbstractKey {
    public static final MunchContainerKey updatedDate = new MunchContainerKey("updatedDate", false);
    public static final MunchContainerKey sourceCorpusName = new MunchContainerKey("sourceCorpusName", false);
    public static final MunchContainerKey sourceCorpusKey = new MunchContainerKey("sourceCorpusKey", false);

    private MunchContainerKey(String key, boolean multi) {
        super("Sg.Munch.Container." + key, multi);
    }

    public boolean equal(CorpusData data, Date date, long dataVersion) {
        String right = Long.toString(date.getTime() + dataVersion);
        return StringUtils.equals(getValueOrThrow(data), right);
    }
}
