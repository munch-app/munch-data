package munch.data.place.graph.linker;

import corpus.data.CorpusData;
import corpus.field.FieldUtils;

import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 30/3/2018
 * Time: 10:44 PM
 * Project: munch-data
 */
public class PlaceIdLinker implements Linker {

    @Override
    public String getName() {
        return "PlaceIdLinker";
    }

    @Override
    public boolean link(Map<String, Integer> matchers, CorpusData left, CorpusData right) {
        if (left.getCorpusName().equals("Sg.Munch.Place")) {
            String rightPlaceId = FieldUtils.getValue(right, "Place.id");
            if (rightPlaceId == null) return false;

            return left.getCorpusKey().equals(rightPlaceId);

        }
        return false;
    }
}
