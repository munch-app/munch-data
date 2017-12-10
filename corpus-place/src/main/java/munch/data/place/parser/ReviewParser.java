package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.FieldUtils;
import munch.data.structure.Place;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 17/11/17
 * Time: 8:23 PM
 * Project: munch-data
 */
@Singleton
public final class ReviewParser extends AbstractParser<Place.Review> {

    /**
     * Find and parse review data
     * Sources:
     * - Facebook
     *
     * @param place read-only place data
     * @param list  list of corpus data to parse from
     * @return Place.Review data type
     */
    @Override
    @Nullable
    public Place.Review parse(Place place, List<CorpusData> list) {
        CorpusData facebookData = find(list, "Global.Facebook.Place");
        if (facebookData == null) return null;

        int total = FieldUtils.get(facebookData, "Global.Facebook.Place.ratingCount", fields -> {
            if (fields.isEmpty()) return 0;
            return Integer.parseInt(fields.get(0).getValue());
        });
        if (total == 0) return null;

        double average = FieldUtils.get(facebookData, "Global.Facebook.Place.overallStarRating", fields -> {
            if (fields.isEmpty()) return 0.0;
            return Double.parseDouble(fields.get(0).getValue()) / 5;
        });
        if (average == 0.0) return null;

        Place.Review review = new Place.Review();
        review.setTotal(total);
        review.setAverage(average);
        return review;
    }
}
