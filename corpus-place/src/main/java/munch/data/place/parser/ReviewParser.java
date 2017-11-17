package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.FieldUtils;
import munch.data.structure.Place;

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
    public Place.Review parse(Place place, List<CorpusData> list) {
        CorpusData facebookData = find(list, "Global.Facebook.Place");
        if (facebookData == null) return new Place.Review();

        Place.Review review = new Place.Review();
        int total = FieldUtils.get(facebookData, "Global.Facebook.Place.ratingCount", fields -> {
            if (fields.isEmpty()) return 0;
            return Integer.parseInt(fields.get(0).getValue());
        });
        double average = FieldUtils.get(facebookData, "Global.Facebook.Place.overallStarRating", fields -> {
            if (fields.isEmpty()) return 0.0;
            return Double.parseDouble(fields.get(0).getValue());
        });
        review.setTotal(total);
        review.setAverage(average);
        return review;
    }
}
