package munch.data.linking;

import java.util.List;

/**
 * Created by: Fuxing
 * Date: 26/3/2018
 * Time: 6:28 AM
 * Project: munch-data
 */
public class EatigoPlatform implements Platform {

    @Override
    public String parse(PlatformUrl url) {
        List<String> paths = url.getPaths();
        if (paths.size() < 7) return null;
        if (!paths.get(0).equals("home")) return null;
        if (!paths.get(1).equals("index.php")) return null;

        return wrap("eatigo.com/", "/", url.getPath(2), url.getPath(4), url.getPath(6));
    }
}
