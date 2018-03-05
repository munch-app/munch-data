package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import corpus.utils.FieldCollector;
import munch.data.structure.Place;
import org.apache.commons.lang3.StringUtils;

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
        FieldCollector fieldCollector = new FieldCollector(PlaceKey.price);
        fieldCollector.addAll(list);

        String priorityPrice = fieldCollector.collectMax(priorityCorpus);
        if (priorityPrice != null) {
            Double price = clean(priorityPrice);
            if (price != null) {
                return create(price);
            }
        }

        List<Double> prices = fieldCollector.collectField().stream()
                .map(field -> clean(field.getValue()))
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
        value = (double) Math.round(value);
        if (value > 200) value = 200.0;
        price.setMiddle(value);
        return price;
    }

    @Nullable
    public static Double clean(String value) {
        if (StringUtils.isBlank(value)) return null;

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
