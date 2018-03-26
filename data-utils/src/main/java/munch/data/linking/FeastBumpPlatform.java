package munch.data.linking;

/**
 * Created by: Fuxing
 * Date: 26/3/2018
 * Time: 6:50 AM
 * Project: munch-data
 */
public class FeastBumpPlatform implements Platform {
    @Override
    public String parse(PlatformUrl url) {
        if (url.hasPath(2, "menus")) {
            return wrap("feastbump.com/", url.getPath(1));
        }
        return null;
    }
}
