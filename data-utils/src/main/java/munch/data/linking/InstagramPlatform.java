package munch.data.linking;

/**
 * Created by: Fuxing
 * Date: 26/3/2018
 * Time: 6:28 AM
 * Project: munch-data
 */
public class InstagramPlatform implements Platform {

    @Override
    public String parse(PlatformUrl url) {
        return wrap("instagram.com/", url.getPath(0));
    }
}
