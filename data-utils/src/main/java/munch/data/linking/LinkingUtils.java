package munch.data.linking;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 30/3/2018
 * Time: 10:24 PM
 * Project: munch-data
 */
public final class LinkingUtils {

    public static List<String> getPrefix(String prefix, List<String> linkings) {
        if (linkings.isEmpty()) return List.of();

        List<String> list = new ArrayList<>();
        for (String linking : linkings) {
            if (linking.startsWith(prefix)) list.add(linking);
        }

        return list;
    }

}
