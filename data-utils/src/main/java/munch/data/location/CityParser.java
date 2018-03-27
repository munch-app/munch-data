package munch.data.location;

import javax.annotation.Nullable;

/**
 * Created by: Fuxing
 * Date: 27/3/2018
 * Time: 1:38 AM
 * Project: munch-data
 */
public interface CityParser {

    /**
     * @param text text to parse
     * @return LocationData if found
     */
    @Nullable
    LocationData parse(String text);
}
