package munch.data.place;

import corpus.data.CorpusData;
import corpus.field.AbstractKey;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by: Fuxing
 * Date: 6/3/18
 * Time: 5:36 PM
 * Project: munch-data
 */
public final class PlaceAwardKey extends AbstractKey {

    public static final PlaceAwardKey awardName = new PlaceAwardKey("PlaceAward.awardName", false);
    public static final PlaceAwardKey sort = new PlaceAwardKey("PlaceAward.sort", false);

    public static final PlaceAwardKey status = new PlaceAwardKey("PlaceAward.status", false);
    public static final PlaceAwardKey placeId = new PlaceAwardKey("PlaceAward.placeId", false);

    protected PlaceAwardKey(String key, boolean multi) {
        super(key, multi);
    }

    public long getLong(CorpusData data) {
        return Long.parseLong(getValueOrThrow(data));
    }

    public static String getSortKey(CorpusData data, CorpusData.Field field) {
        long collectionId = Optional.ofNullable(field.getMetadata())
                .flatMap(map -> Optional.ofNullable(map.get("CollectionId")))
                .map(Long::parseLong)
                .orElseThrow(NullPointerException::new);

        return new UUID(collectionId, sort.getLong(data)).toString();
    }
}
