package munch.data.place.graph.matcher;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 30/3/2018
 * Time: 11:22 PM
 * Project: munch-data
 */
@Singleton
public final class LinkingMatcher implements Matcher {
    private static final List<String> BLOCKED_PREFIX = List.of(
            "facebook.com/",
            "google.com/",
            "instagram.com/"
    );

    @Override
    public Map<String, Integer> match(String placeId, CorpusData left, CorpusData right) {
        Set<String> linkingLeft = getLinkings(left);
        Set<String> linkingRight = getLinkings(right);
        if (linkingLeft.isEmpty() || linkingRight.isEmpty()) return Map.of();

        linkingLeft.retainAll(linkingRight);
        return Map.of("Place.linking", linkingLeft.size());
    }

    @Override
    public Set<String> requiredFields() {
        return Set.of("Place.linking");
    }

    public static Set<String> getLinkings(CorpusData data) {
        List<String> linkings = PlaceKey.linking.getAllValue(data);
        if (linkings.isEmpty()) return Set.of();

        linkings.removeIf(linking -> {
            for (String prefix : BLOCKED_PREFIX) {
                if (linking.startsWith(prefix)) return true;
            }
            return false;
        });
        return new HashSet<>(linkings);
    }
}
