package munch.data.place.parser;

import catalyst.utils.LatLngUtils;
import org.junit.jupiter.api.Test;

/**
 * Created by: Fuxing
 * Date: 16/10/2017
 * Time: 7:48 PM
 * Project: munch-data
 */
class LocationParserTest {

    @Test
    void geocode() throws Exception {
        OneMapApi oneMapApi = new OneMapApi();
        LatLngUtils.LatLng geocode = oneMapApi.geocode("570192");
        System.out.println(geocode);
    }
}