package munch.data.place.graph.linker;

import corpus.data.CorpusData;
import munch.data.place.graph.PlaceTree;

import javax.inject.Singleton;
import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 31/3/18
 * Time: 12:32 PM
 * Project: munch-data
 */
@Singleton
public final class LocationPhoneWebsiteLinker implements Linker {
    @Override
    public String getName() {
        return "LocationPhoneWebsiteLinker";
    }

    @Override
    public boolean link(String placeId, PlaceTree left, Map<String, Integer> matchers, CorpusData right) {
        int postal = matchers.getOrDefault("Place.Location.postal", 0);
        int phone = matchers.getOrDefault("Place.phone", 0);
        int website = matchers.getOrDefault("Place.website", 0);

        return phone >= 1 && postal >= 1 && website >= 1;
    }
}
