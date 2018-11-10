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
        Double perPax = findPerPax(mutation.getMenuPricePerPax());
        if (perPax == null) return null;

        Place.Price price = new Place.Price();
        price.setPerPax(perPax);
        return price;
    }

    /**
     * @param list of field to find
     * @return per pax found
     */
    private Double findPerPax(List<MutationField<Double>> list) {
        if (list.isEmpty()) return null;

        for (MutationField<Double> perPax : list) {
            if (perPax.getValue() <= 200.0 && perPax.getValue() > 0.0) return perPax.getValue();
        }

        if (list.get(0).getValue() <= 0) return null;
        if (list.get(0).getValue() > 200) return 200d;
        return null;
    }
}
