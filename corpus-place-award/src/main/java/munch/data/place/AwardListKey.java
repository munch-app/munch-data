package munch.data.place;

import corpus.data.CorpusData;
import corpus.field.AbstractKey;

import java.util.UUID;

/**
 * Created by: Fuxing
 * Date: 6/3/18
 * Time: 5:27 PM
 * Project: munch-data
 */
public final class AwardListKey extends AbstractKey {

    public static final AwardListKey awardName = new AwardListKey("AwardList.awardName");
    public static final AwardListKey userId = new AwardListKey("AwardList.userId");
    public static final AwardListKey collectionId = new AwardListKey("AwardList.collectionId");

    protected AwardListKey(String key) {
        super(key, false);
    }

    public long getLong(CorpusData data) {
        return Long.parseLong(getValueOrThrow(data));
    }

    public String getUUID(CorpusData data) {
        return new UUID(getLong(data), 0).toString();
    }
}
