package munch.data.place.parser.hour;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.structure.Place;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Created by: Fuxing
 * Date: 22/12/2017
 * Time: 10:56 PM
 * Project: munch-data
 */
class CorpusDataHourTest {

    @Test
    void test() throws Exception {
        List<CorpusData.Field> fieldList = List.of(
                PlaceKey.Hour.mon.create("15:00", "18:00"),
                PlaceKey.Hour.mon.create("18:00", "19:00"),
                PlaceKey.Hour.tue.create("22:00", "02:00"),
                PlaceKey.Hour.wed.create("10:00", "15:00")
        );
        CorpusDataHour hour = new CorpusDataHour("", "", fieldList);
        hour.getDays().forEach((day, dayOpenClose) -> {
            System.out.println("Day: "+ day);
            for (Place.Hour placeHour : dayOpenClose.getPlaceHours()) {
                System.out.println(placeHour.getOpen() + " - " + placeHour.getClose());
            }
        });
    }
}