package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.structure.Place;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.util.Collections;
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
public final class PriceParser extends AbstractParser {

    @Nullable
    public Place.Price parse(List<CorpusData> list) {
        List<CorpusData.Field> fields = collect(list, PlaceKey.price);
        List<Double> prices = fields.stream()
                .map(PriceParser::clean)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (prices.isEmpty()) return null;
        double low = Collections.min(prices);
        double high = Collections.max(prices);

        Place.Price price = new Place.Price();
        price.setLowest(low);
        price.setHighest(high);
        price.setMiddle((high - low) / 2 + low);
        return price;
    }

    @Nullable
    public static Double clean(CorpusData.Field field) {
        String value = field.getValue();
        String cleaned = value.replace("$", "")
                .replace(" ", "");

        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
