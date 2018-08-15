package munch.data.catalyst;

import catalyst.mutation.PlaceMutation;
import catalyst.mutation.PlaceMutationClient;
import munch.data.client.PlaceClient;
import munch.data.place.Place;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 12/8/18
 * Time: 2:09 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceValidator implements Runnable {
    private final PlaceClient placeClient;
    private final PlaceMutationClient mutationClient;

    @Inject
    public PlaceValidator(PlaceClient placeClient, PlaceMutationClient mutationClient) {
        this.placeClient = placeClient;
        this.mutationClient = mutationClient;
    }


    @Override
    public void run() {
        placeClient.iterator().forEachRemaining(this::validate);
    }

    private void validate(Place place) {
        PlaceMutation mutation = mutationClient.get(place.getPlaceId());
        if (mutation == null) {
            // If mutation don't exist, delete
            placeClient.delete(place.getPlaceId());
        }
    }
}
