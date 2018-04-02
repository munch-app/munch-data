package munch.data.linking;

/**
 * Created by: Fuxing
 * Date: 26/3/2018
 * Time: 6:27 AM
 * Project: munch-data
 */
public class FacebookPlatform implements Platform {

    @Override
    public String parse(PlatformUrl url) {

        return wrap("facebook.com/", url.getPath());
    }
}
