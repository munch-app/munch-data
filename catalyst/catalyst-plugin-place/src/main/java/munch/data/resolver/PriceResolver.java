package munch.data.resolver;

import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import munch.data.place.Place;

import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 12/8/18
 * Time: 1:59 PM
 * Project: munch-data
 */
@Singleton
public final class PriceResolver {

    public Place.Price resolve(PlaceMutation mutation) {
        List<MutationField<Double>> perPax = mutation.getMenuPricePerPax();
        if (perPax.isEmpty()) return null;


        Place.Price price = new Place.Price();
        price.setPerPax(perPax.get(0).getValue());
        return price;
    }
}
