package munch.data.catalyst;

import catalyst.mutation.PlaceMutation;
import catalyst.mutation.PlaceMutationClient;
import catalyst.plugin.CorePlugin;
import catalyst.source.SourceReporter;
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
public final class PlaceValidator implements CorePlugin {
    private final PlaceClient placeClient;
    private final PlaceMutationClient mutationClient;

    @Inject
    public PlaceValidator(PlaceClient placeClient, PlaceMutationClient mutationClient) {
        this.placeClient = placeClient;
        this.mutationClient = mutationClient;
    }

    private void validate(Place place) {
        PlaceMutation mutation = mutationClient.get(place.getPlaceId());
        if (mutation == null) {
            // If mutation don't exist, delete
            placeClient.delete(place.getPlaceId());
        }
    }

    @Override
    public String getSource() {
        return "validate.data.munch.space";
    }

    @Override
    public void run(SourceReporter.Session session) {
        placeClient.iterator().forEachRemaining(this::validate);
    }
}
