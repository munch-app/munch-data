package munch.data;

import catalyst.mutation.PlaceMutation;
import catalyst.mutation.PlaceMutationClient;
import catalyst.utils.SleepUtils;
import munch.data.client.PlaceClient;
import munch.data.place.Place;
import munch.data.resolver.LocationSupportException;
import munch.data.resolver.ResolverHaltException;
import munch.restful.core.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Duration;

/**
 * Created by: Fuxing
 * Date: 18/10/18
 * Time: 5:25 PM
 * Project: munch-feed
 */
public class PlaceWorker implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(PlaceWorker.class);
    public static final Duration BETWEEN_BLOCK = Duration.ofSeconds(60);

    private final PlaceQueue queue;
    private final PlaceParser placeParser;
    private final PlaceMutationClient mutationClient;
    private final PlaceClient placeClient;

    @Inject
    protected PlaceWorker(PlaceParser placeParser, PlaceQueue queue, PlaceMutationClient mutationClient, PlaceClient placeClient) {
        this.placeParser = placeParser;
        this.queue = queue;
        this.mutationClient = mutationClient;
        this.placeClient = placeClient;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            boolean hasAny = queue.consume(this::consume);
            if (!hasAny) {
                logger.info("Sleeping between block.");
                SleepUtils.sleep(BETWEEN_BLOCK);
            }
        }
    }

    private void consume(PlaceQueue.Body body) {
        switch (body.getType()) {
            case Put:
                put(body.getPlaceId(), body.getMillis());
                break;
            case Delete:
                delete(body.getPlaceId());
                break;
        }
    }

    public void put(String placeId, long millis) {
        Place existing = placeClient.get(placeId);
        if (existing != null && existing.getUpdatedMillis() >= millis) {
            logger.info("Put is not required, a newer version is already persisted.");
            return;
        }

        PlaceMutation mutation = mutationClient.get(placeId);
        if (mutation == null) {
            logger.warn("Put but don't exist, trying to deleting now.");
            delete(placeId);
            return;
        }

        Place parsed = parse(mutation);
        if (parsed != null) {
            placeClient.put(parsed);
        } else if (existing != null) {
            delete(placeId);
        }
    }

    @SuppressWarnings("Duplicates")
    public void delete(String placeId) {
        Place place = placeClient.get(placeId);
        if (place == null) {
            logger.warn("Deleted Not Found: {}", placeId);
            return;
        }

        Place.Status status = new Place.Status();
        status.setType(Place.Status.Type.deleted);
        place.setStatus(status);

        logger.info("Deleted: {}", place);
    }

    /**
     * @param mutation to parse into place
     * @return null if parsed failed
     */
    private Place parse(PlaceMutation mutation) {
        try {
            Place place = placeParser.parse(mutation);
            ValidationException.validate(place);
            return place;
        } catch (LocationSupportException e) {
            logger.warn("Location not supported for {}", mutation.getPlaceId(), e);
        } catch (ValidationException e) {
            logger.warn("Validation failed", e);
            logger.warn("Mutation Id: {}, Data: {}", mutation.getPlaceId(), mutation);
        } catch (ResolverHaltException ignore) {
        }
        return null;
    }
}
