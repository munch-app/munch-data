package munch.data.place.graph.seeder;

import munch.data.place.graph.PlaceTree;

import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 10:50 PM
 * Project: munch-data
 */
public final class TrustedSeeder implements Seeder {

    public static final Set<String> TRUSTED_SEEDER = Set.of(
            "Sg.MunchSheet.PlaceInfo2",
            "Sg.MunchUGC.PlaceSuggest",
            "Sg.Nea.TrackRecord",
            "Sg.Munch.PlaceAward"
    );

    /**
     * @param placeTree tree to try seed
     * @return true if tree contains a trusted source
     */
    @Override
    public Result trySeed(String placeId, PlaceTree placeTree) {
        for (String corpusName : placeTree.getCorpusNames()) {
            if (TRUSTED_SEEDER.contains(corpusName)) return Result.Seed;
        }

        return Result.Proceed;
    }

}
