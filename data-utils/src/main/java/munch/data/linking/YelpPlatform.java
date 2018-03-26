package munch.data.linking;

/**
 * Created by: Fuxing
 * Date: 26/3/2018
 * Time: 6:27 AM
 * Project: munch-data
 */
public class YelpPlatform implements Platform {

    @Override
    public String parse(PlatformUrl url) {
        if (url.hasPath(2, "biz")) {
            return wrap("yelp.com/biz/", url.getPath(1));
        }
        return null;
    }
}
