package munch.data.catalyst;

import catalyst.utils.retriable.Retriable;
import catalyst.utils.retriable.TimeoutRetriable;
import munch.data.place.Place;
import munch.taste.GlobalTaste;
import munch.taste.GlobalTasteClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
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
    private static final Retriable retriable = new TimeoutRetriable(Duration.ofSeconds(20));

    private static final Place.Taste DEFAULT;

    static {
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
        GlobalTaste taste = getGlobalTaste(place);
        if (taste == null) return DEFAULT;

        Place.Taste placeTaste = new Place.Taste();
        placeTaste.setGroup(taste.getGroup());
        placeTaste.setImportance(taste.getImportance());
        return placeTaste;
    }

    private GlobalTaste getGlobalTaste(Place place) {
        Map<String, GlobalTaste> tasteMap = retriable.loop(() ->
                tasteClient.post(List.of(place))
        );
        return tasteMap.get(place.getPlaceId());
    }
}
