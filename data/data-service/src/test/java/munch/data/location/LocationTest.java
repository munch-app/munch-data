package munch.data.location;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by: Fuxing
 * Date: 27/8/2018
 * Time: 7:49 PM
 * Project: munch-data
 */
class LocationTest {

    @Test
    void city() throws Exception {
        City city;
        try {
            city = City.valueOf("abc");
        }catch (IllegalArgumentException e) {
            city = null;
        }

        System.out.println(city);
    }
}