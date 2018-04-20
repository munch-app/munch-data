package munch.data.place.graph.linker;

import corpus.data.CorpusData;
import munch.data.place.graph.PlaceTree;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 10:04 PM
 * Project: munch-data
 */
@Singleton
public final class LinkerManager {
    private final List<Linker> linkers = List.of(
            new PlaceIdLinker(),
            new FacebookLinker(),
            new PostalNameLinker(),
            new SpatialNameLinker(),
            new LocationPhoneLinkingLinker(),
            new LocationPhoneWebsiteLinker(),
            new LocationPhoneUnitLinker(),
            new ContainNameLinker()
    );

    private final Map<String, Linker> linkerMap;

    @Inject
    public LinkerManager() {
        linkerMap = new HashMap<>();
        for (Linker linker : linkers) {
            linkerMap.put(linker.getName(), linker);
        }
    }

    /**
     * @param matchers is matcher data from matcher package
     * @param left     corpus data
     * @param right    corpus data, in tree
     * @return Optional String, of linked using which linker
     */
    public Optional<String> link(String placeId, PlaceTree left,  Map<String, Integer> matchers, CorpusData right) {
        for (Linker linker : linkers) {
            if (linker.link(placeId, left, matchers, right)) {
                return Optional.of(linker.getName());
            }
        }
        return Optional.empty();
    }

    /**
     * @param linkerName to find linker and use
     * @param matchers   is matcher data from matcher package
     * @param left       corpus data
     * @param right      corpus data, in tree
     * @return validation result
     */
    public boolean validate(String linkerName,String placeId, PlaceTree left, Map<String, Integer> matchers, CorpusData right) {
        Linker linker = linkerMap.get(linkerName);
        if (linker == null) return false;

        return linker.link(placeId, left, matchers, right);
    }

}
