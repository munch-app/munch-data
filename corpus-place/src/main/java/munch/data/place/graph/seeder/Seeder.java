package munch.data.place.graph.seeder;

import munch.data.place.graph.PlaceTree;

/**
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 10:03 PM
 * Project: munch-data
 */
public interface Seeder {
    enum Result {
        Seed,       // Can be seeded
        Block,      // Remove from database
        Decayed,    // Data has decayed, mark as deleted
        Proceed,    // No conclusion, move to next seeder
    }


    /**
     * @param placeTree tree to try seed
     * @return whether successfully seeded
     */
    Result trySeed(String placeId, PlaceTree placeTree);

}
