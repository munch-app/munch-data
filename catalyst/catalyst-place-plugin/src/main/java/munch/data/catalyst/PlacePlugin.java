package munch.data.catalyst;

import catalyst.mutation.PlaceMutation;
import catalyst.plugin.CollectPlugin;
import munch.data.client.PlaceClient;
import munch.data.place.Place;
import munch.data.resolver.LocationSupportException;
import munch.restful.core.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 8/8/18
 * Time: 4:32 PM
 * Project: munch-data
 */
@Singleton
public final class PlacePlugin extends CollectPlugin {
    private static final Logger logger = LoggerFactory.getLogger(PlacePlugin.class);

    private final PlaceParser placeParser;
    private final PlaceClient placeClient;

    @Inject
    public PlacePlugin(PlaceParser placeParser, PlaceClient placeClient) {
        this.placeParser = placeParser;
        this.placeClient = placeClient;
    }

    @Override
    protected void each(PlaceMutation placeMutation) {
        try {
            Place place = placeParser.parse(placeMutation);
            ValidationException.validate(place);
            placeClient.put(place);
        } catch (LocationSupportException e) {
            logger.warn("Location not supported for {}", placeMutation.getPlaceId(), e);
        }
    }
}
