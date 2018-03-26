package munch.data.linking;

import java.util.List;

/**
 * Created by: Fuxing
 * Date: 26/3/2018
 * Time: 6:27 AM
 * Project: munch-data
 */
public class DeliverooPlatform implements Platform {

    @Override
    public String parse(PlatformUrl url) {
        if (url.hasPath(4, "menu")) {
            List<String> paths = url.getPaths();

            return wrap("deliveroo.com/", "/", paths.get(1), paths.get(2), paths.get(3));
        }
        return null;
    }

}
