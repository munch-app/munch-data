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
            Place.Price price = new Place.Price();
            price.setMiddle(secondLast);
            return price;
        }

        // More then 10 prices
        int index = (int) (((double) prices.size()) * 0.7);
        Place.Price price = new Place.Price();
        price.setMiddle(prices.get(index));
        return price;
    }

    @Nullable
    public static Double clean(CorpusData.Field field) {
        String value = field.getValue();
        String cleaned = value
                .replace("$", "")
                .replace(" ", "");

        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
