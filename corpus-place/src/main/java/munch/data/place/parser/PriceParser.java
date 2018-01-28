package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.structure.Place;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 10:10 PM
 * Project: munch-data
 */
@Singleton
public final class PriceParser extends AbstractParser<Place.Price> {

    @Nullable
    @Override
    public Place.Price parse(Place place, List<CorpusData> list) {
        List<CorpusData.Field> fields = collect(list, PlaceKey.price);
        List<Double> prices = fields.stream()
                .map(PriceParser::clean)
                .filter(Objects::nonNull)
                .sorted(Double::compareTo)
                .collect(Collectors.toList());

        if (prices.isEmpty()) return null;

        // If only less then 10 prices
        if (prices.size() < 10) {
            Double secondLast = prices.get(prices.size() - 1);
            return create(secondLast);
        }

        // More then 10 prices
        int index = (int) (((double) prices.size()) * 0.7);
        return create(prices.get(index));
    }

    private static Place.Price create(double value) {
        Place.Price price = new Place.Price();
        value = value * 1.17;
        price.setMiddle((double) Math.round(value));
        return price;
    }

    @Nullable
    public static Double clean(CorpusData.Field field) {
        String value = field.getValue();
        String cleaned = value
                .replace("$", "")
                .replace(" ", "");

        try {
            double price = Double.parseDouble(cleaned);
            if (price <= 200) return price;

            // Extreme prices are removed
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
