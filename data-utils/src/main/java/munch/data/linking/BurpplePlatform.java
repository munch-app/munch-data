package munch.data.linking;

/**
 * Created by: Fuxing
 * Date: 26/3/2018
 * Time: 6:28 AM
 * Project: munch-data
 */
public class BurpplePlatform implements Platform {

    @Override
    public String parse(PlatformUrl url) {
        if (url.getPaths().size() == 1) {
            return wrap("burrple.com/place/", url.getPath(0));
        }
        return null;
    }
}
