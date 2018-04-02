package munch.data.place.graph.linker;

import corpus.data.CorpusData;
import corpus.field.FieldUtils;
import munch.data.place.graph.PlaceTree;

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
    public boolean link(String placeId, PlaceTree left, Map<String, Integer> matchers, CorpusData right) {
        String rightPlaceId = FieldUtils.getValue(right, "Place.id");

        return placeId.equals(rightPlaceId);
    }
}
