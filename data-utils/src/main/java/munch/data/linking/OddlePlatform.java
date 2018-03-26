package munch.data.linking;

import java.util.List;

/**
 * Created by: Fuxing
 * Date: 26/3/2018
 * Time: 6:28 AM
 * Project: munch-data
 */
public class OddlePlatform implements Platform {

    @Override
    public String parse(PlatformUrl url) {
        List<String> parts = url.getDomainParts();
        if (parts.size() != 3) return null;

        return wrap("oddle.me/site/", parts.get(0));
    }
}
