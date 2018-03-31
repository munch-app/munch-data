package munch.data.place.graph.seeder;

import munch.data.place.graph.PlaceTree;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 10:04 PM
 * Project: munch-data
 */
public final class SeederManager {
    private final List<Seeder> seederList;

    @Inject
    public SeederManager(ValidationSeeder validationSeeder, DecaySeeder decaySeeder, TrustedSeeder trustedSeeder) {
        this.seederList = List.of(validationSeeder, decaySeeder, trustedSeeder);
    }

    /**
     * @param placeTree tree to try seed
     * @return whether it is successfully seeded
     */
    public Seeder.Result trySeed(String placeId, PlaceTree placeTree) {
        for (Seeder seeder : seederList) {
            Seeder.Result result = seeder.trySeed(placeId, placeTree);
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
