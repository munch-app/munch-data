package munch.data.place;

import munch.data.Location;
import munch.restful.core.JsonUtils;
import munch.restful.core.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlaceTest {
    @Test
    void json() {
        Place place = new Place();
        place.setName("BH");
        place.setLocation(new Location());
        place.getLocation().setAddress("House");

        String json = JsonUtils.toString(place);
        System.out.println(json);
    }
}