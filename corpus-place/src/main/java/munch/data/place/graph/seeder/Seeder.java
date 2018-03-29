package munch.data.place.graph.seeder;

import munch.data.place.graph.PlaceTree;

/**
 * Created by: Fuxing
 * Date: 29/3/18
 * Time: 10:03 PM
 * Project: munch-data
 */
public interface Seeder {

    /**
     * @param placeTree tree to try seed
     * @return whether successfully seeded
     */
    boolean trySeed(PlaceTree placeTree);

}
