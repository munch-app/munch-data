package munch.data.place.parser.hour;

import munch.data.structure.Place;
import org.junit.jupiter.api.Test;

/**
 * Created by: Fuxing
 * Date: 22/12/2017
 * Time: 10:45 PM
 * Project: munch-data
 */
class DayOpenCloseTest {

    @Test
    void name() throws Exception {
        DayOpenClose openClose = new DayOpenClose();
        openClose.put("8:00", "14:45");
        openClose.put("15:00", "22:00");

        for (Place.Hour hour : openClose.getPlaceHours()) {
            System.out.println(hour.getOpen() + " - " + hour.getClose());
        }
    }
}