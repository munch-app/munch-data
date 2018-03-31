package munch.data.place.graph.seeder;

import corpus.field.AbstractKey;

/**
 * Created by: Fuxing
 * Date: 1/4/2018
 * Time: 2:37 AM
 * Project: munch-data
 */
public class DecayingKey extends AbstractKey {
    public static final DecayingKey startMillis = new DecayingKey("startMillis");
    public static final DecayingKey endMillis = new DecayingKey("endMillis");
    public static final DecayingKey name = new DecayingKey("name");

    protected DecayingKey(String key) {
        super("Decaying." + key, false);
    }
}
