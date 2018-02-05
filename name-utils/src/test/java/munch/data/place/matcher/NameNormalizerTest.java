package munch.data.place.matcher;

import org.junit.jupiter.api.Test;

/**
 * Created by: Fuxing
 * Date: 5/2/2018
 * Time: 3:10 PM
 * Project: munch-data
 */
class NameNormalizerTest {

    NameNormalizer nameNormalizer = new NameNormalizer();

    @Test
    void name() throws Exception {
        String name = nameNormalizer.normalize("Place &amp; at is good");
        System.out.println(name);
    }
}