package munch.data.location;

import corpus.field.AbstractKey;

/**
 * Created by: Fuxing
 * Date: 4/10/17
 * Time: 5:06 PM
 * Project: munch-corpus
 */
public final class LocationKey extends AbstractKey {

    public static final LocationKey name = new LocationKey("name");
    public static final LocationKey country = new LocationKey("country");
    public static final LocationKey city = new LocationKey("city");

    public static final LocationKey polygon = new LocationKey("polygon");
    public static final LocationKey latLng = new LocationKey("latLng");


    private LocationKey(String key) {
        super("Location." + key, false);
    }
}
