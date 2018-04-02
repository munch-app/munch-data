package munch.data.place.graph.linker;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.place.graph.PlaceTree;

import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 31/3/18
 * Time: 2:25 PM
 * Project: munch-data
 */
public class NeaNoNameLinker implements Linker {
    @Override
    public String getName() {
        return "NeaNoNameLinker";
    }

    @Override
    public boolean link(String placeId, PlaceTree left, Map<String, Integer> matchers, CorpusData right) {
        int postal = matchers.getOrDefault("Place.Location.postal", 0);
        if (postal < 1) return false;

        if (!left.getCorpusData().getCorpusName().equals("Sg.Nea.TrackRecord")) return false;
        if (PlaceKey.name.has(left.getCorpusData())) return false;


        // TODO Multiple Rights
        // For NeaNoName & ArticleToArticle
        return false;
    }
}