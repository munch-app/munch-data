package munch.data.resolver;

import catalyst.mutation.PlaceMutation;
import munch.data.place.Place;

import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 8/8/18
 * Time: 4:35 PM
 * Project: munch-data
 */
@Singleton
public final class BrandResolver {

    public Place.Brand resolve(PlaceMutation mutation) {
        // Need to wait for Brand Plugin to be completed
        return null;
    }
}
