package munch.data.linking;

/**
 * Created by: Fuxing
 * Date: 26/3/2018
 * Time: 6:27 AM
 * Project: munch-data
 */
public class FoursquarePlatform implements Platform {

    @Override
    public String parse(PlatformUrl url) {
        if (url.hasPath(3, "v")) {
            return wrap("foursquare.com/v/", url.getPath(2));
        }
        return null;
    }
}
