package munch.data.location;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 27/3/2018
 * Time: 1:42 AM
 * Project: munch-data
 */
@Singleton
public final class LibpostalParser extends CityParser {

    @Nullable
    @Override
    public LocationData parse(String text) {
        return null;
    }

    @Nullable
    @Override
    public LocationData parse(List<String> tokens) {
        return null;
    }

}
