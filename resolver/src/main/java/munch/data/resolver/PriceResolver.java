package munch.data.resolver;

import catalyst.mutation.MenuItemCollection;
import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import munch.data.place.Place;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.util.ArrayList;
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
        Double perPax = findMenuPricePerPax(mutation);
        if (perPax != null) return parse(perPax);

        perPax = findMenuItemPrice(mutation);
        if (perPax != null) return parse(perPax);

        return null;
    }

    /**
     * @param mutation to find from
     * @return per pax found
     */
    @Nullable
    private Double findMenuPricePerPax(PlaceMutation mutation) {
        List<MutationField<Double>> list = mutation.getMenuPricePerPax();
        if (list.isEmpty()) return null;

        for (MutationField<Double> perPax : list) {
            if (perPax.getValue() <= 200.0 && perPax.getValue() > 0.0) return perPax.getValue();
        }

        return list.get(0).getValue();
    }

    private Double findMenuItemPrice(PlaceMutation mutation) {
        List<MutationField<MenuItemCollection>> items = mutation.getMenuItem();
        if (items == null) return null;
        if (items.isEmpty()) return null;

        List<Double> graph = new ArrayList<>();
        items.forEach(field -> graph.addAll(field.getValue().getPrices()));

        if (graph.size() < 8) return null;
        graph.sort(Double::compareTo);

        int index = (int) (((double) graph.size()) * 0.75);
        return graph.get(index) * 1.05;
    }

    private static Place.Price parse(Double value) {
        if (value == null) return null;

        if (value <= 0) return null;
        if (value > 200) value = 200d;

        Place.Price price = new Place.Price();
        price.setPerPax(Math.ceil(value));
        return price;
    }
}
