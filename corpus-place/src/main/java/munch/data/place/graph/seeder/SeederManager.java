package munch.data.place.graph.seeder;

import munch.data.place.graph.PlaceTree;

import java.util.List;

/**
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 10:04 PM
 * Project: munch-data
 */
public final class SeederManager {
    private final List<Seeder> seederList = List.of(
            new TrustedSeeder()
    );

    /**
     * @param placeTree tree to try seed
     * @return whether it is successfully seeded
     */
    public boolean trySeed(PlaceTree placeTree) {
        for (Seeder seeder : seederList) {
            if (seeder.trySeed(placeTree)) return true;
        }
        return false;
    }
}
