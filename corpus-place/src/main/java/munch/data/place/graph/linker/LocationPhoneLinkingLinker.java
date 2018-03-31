package munch.data.place.graph.linker;

import corpus.data.CorpusData;

import javax.inject.Singleton;
import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 31/3/18
 * Time: 12:32 PM
 * Project: munch-data
 */
@Singleton
public final class LocationPhoneLinkingLinker implements Linker {
    @Override
    public String getName() {
        return "LocationPhoneLinkingLinker";
    }

    @Override
    public boolean link(Map<String, Integer> matchers, CorpusData left, CorpusData right) {
        int postal = matchers.getOrDefault("Place.Location.postal", 0);
        int phone = matchers.getOrDefault("Place.phone", 0);
        int linking = matchers.getOrDefault("Place.linking", 0);


        return phone >= 1 && postal >= 1 && linking >= 1;
    }
}
