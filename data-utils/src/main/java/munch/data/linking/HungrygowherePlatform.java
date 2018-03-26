package munch.data.linking;

import java.util.List;

/**
 * Created by: Fuxing
 * Date: 26/3/2018
 * Time: 6:28 AM
 * Project: munch-data
 */
public class HungrygowherePlatform implements Platform {

    @Override
    public String parse(PlatformUrl url) {
        List<String> paths = url.getPaths();
        if (paths.size() < 2) return null;

        return wrap("hungrygowhere.com/", "/", paths.get(0), paths.get(1));
    }
}
