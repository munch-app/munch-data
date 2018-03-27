package munch.data.location;

import javax.annotation.Nullable;
import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 27/3/2018
 * Time: 1:42 AM
 * Project: munch-data
 */
@Singleton
public final class LibpostalParser implements CityParser {

    @Nullable
    @Override
    public LocationData parse(String text) {
        // TODO connect api
        return null;
    }

}
