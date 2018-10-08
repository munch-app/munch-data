package munch.data.catalyst;

import munch.data.place.Place;
import munch.taste.GlobalTaste;
import munch.taste.GlobalTasteClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 7/10/18
 * Time: 4:05 PM
 * Project: munch-data
 */
@Singleton
public final class TasteResolver {
    private static final Place.Taste DEFAULT;
    static  {
        DEFAULT = new Place.Taste();
        DEFAULT.setGroup(1);
        DEFAULT.setImportance(0);
    }

    private final GlobalTasteClient tasteClient;

    @Inject
    public TasteResolver(GlobalTasteClient tasteClient) {
        this.tasteClient = tasteClient;
    }

    public Place.Taste resolve(Place place) {
        Map<String, GlobalTaste> tasteMap = tasteClient.post(List.of(place));
        GlobalTaste taste = tasteMap.get(place.getPlaceId());
        if (taste == null) return DEFAULT;

        Place.Taste placeTaste = new Place.Taste();
        placeTaste.setGroup(taste.getGroup());
        placeTaste.setImportance(taste.getImportance());
        return placeTaste;
    }
}
