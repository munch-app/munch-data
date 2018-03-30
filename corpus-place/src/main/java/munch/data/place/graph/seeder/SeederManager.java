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
            new ValidationSeeder(),
            new TrustedSeeder()
    );

    /**
     * @param placeTree tree to try seed
     * @return whether it is successfully seeded
     */
    public Seeder.Result trySeed(PlaceTree placeTree) {
        for (Seeder seeder : seederList) {
            Seeder.Result result = seeder.trySeed(placeTree);
            switch (result) {
                case Seed:
                case Block:
                case Decayed:
                    return result;
                case Proceed:
            }
        }

        // Default to block
        return Seeder.Result.Block;
    }
}
