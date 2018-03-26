package munch.data.linking;

/**
 * Created by: Fuxing
 * Date: 26/3/2018
 * Time: 6:27 AM
 * Project: munch-data
 */
public class HonestBeePlatform implements Platform {

    @Override
    public String parse(PlatformUrl url) {
        if (url.hasPath(4, "en", "food", "restaurants")) {
            return wrap("honestbee.sg/restaurants/", url.getPath(3));
        }
        return null;
    }
}
