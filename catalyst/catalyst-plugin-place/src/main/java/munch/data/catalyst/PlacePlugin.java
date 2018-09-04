package munch.data.catalyst;

import catalyst.mutation.PlaceMutation;
import catalyst.plugin.CollectPlugin;
import catalyst.source.SourceReporter;
import munch.data.client.PlaceClient;
import munch.data.place.Place;
import munch.data.resolver.LocationSupportException;
import munch.data.resolver.ResolverHaltException;
import munch.data.resolver.StatusResolver;
import munch.restful.core.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Iterator;

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

    private final StatusResolver statusResolver;

    @Inject
    public PlacePlugin(PlaceParser placeParser, PlaceClient placeClient, StatusResolver statusResolver) {
        this.placeParser = placeParser;
        this.placeClient = placeClient;
        this.statusResolver = statusResolver;
    }

    @Override
    public String getSource() {
        return "data.munch.space";
    }

    @Override
    protected void receive(PlaceMutation placeMutation) {
        try {
            Place place = placeParser.parse(placeMutation);
            placeClient.put(place);
        } catch (LocationSupportException e) {
            logger.warn("Location not supported for {}", placeMutation.getPlaceId(), e);
        } catch (ValidationException e) {
            logger.warn("Validation failed", e);
            logger.warn("Mutation Id: {}, Data: {}", placeMutation.getPlaceId(), placeMutation);
        } catch (ResolverHaltException ignored) {
            // Place Halted from creation
        }
    }

    @Override
    public void run(SourceReporter.Session session) {
        super.run(session);

        long validated = 0L;
        logger.info("Started validating");
        Iterator<Place> iterator = placeClient.iterator();
        while (iterator.hasNext()) {
            Place place = iterator.next();
            if (isDelete(place)) {
                placeClient.delete(place.getPlaceId());
            }

            if (++validated % 10000L == 0L) {
                logger.info("Validated: {}", validated);
            }
        }
        logger.info("Completed validating: {}", validated);
    }

    private boolean isDelete(Place place) {
        PlaceMutation mutation = placeMutationClient.get(place.getPlaceId());
        // If mutation don't exist, delete
        if (mutation == null) return true;

        try {
            statusResolver.resolve(mutation);
        } catch (ResolverHaltException ignored) {
            return true;
        }

        return false;
    }

}
