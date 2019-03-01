package munch.data.catalyst;

import catalyst.mutation.PlaceMutation;
import catalyst.plugin.CollectPlugin;
import catalyst.source.SourceReporter;
import munch.data.PlaceParser;
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
import java.util.Objects;

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
            Place place = parse(placeMutation);
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

        logger.info("Started Validating");
        placeClient.iterator().forEachRemaining(place -> {
            if (isDelete(place)) {
                deleted(place.getPlaceId());
                counter.increment("Deleted");
            }

            counter.increment("Validated");
        });
    }

    private boolean isDelete(Place place) {
        Objects.requireNonNull(place.getPlaceId());
        PlaceMutation mutation = placeMutationClient.get(place.getPlaceId());
        // If mutation don't exist, delete
        if (mutation == null) return true;

        try {
            statusResolver.resolve(mutation);
            parse(mutation);
            return false;
        } catch (ResolverHaltException | LocationSupportException | ValidationException ignored) {
            return true;
        }
    }

    @SuppressWarnings("Duplicates")
    private void deleted(String placeId) {
        Place place = placeClient.get(placeId);
        if (place == null) {
            logger.warn("Deleted Not Found: {}", placeId);
            return;
        }

        Place.Status status = new Place.Status();
        status.setType(Place.Status.Type.deleted);
        place.setStatus(status);
        placeClient.put(place);
        logger.info("Deleted: {}", place);
    }

    private Place parse(PlaceMutation mutation) throws LocationSupportException, ResolverHaltException, ValidationException {
        Place place = placeParser.parse(mutation);
        ValidationException.validate(place);
        return place;
    }
}
