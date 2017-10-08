package munch.data;

import org.junit.jupiter.api.Test;

/**
 * Created by: Fuxing
 * Date: 17/8/2017
 * Time: 4:27 AM
 * Project: munch-core
 */
class AbstractServiceTest {

    @Test
    void name() throws Exception {
        System.out.println(Place.class.getName());
        System.out.println(Place.class.getTypeName());
        System.out.println(Place.class.getSimpleName());
        System.out.println(Place.class.getCanonicalName());
    }
}