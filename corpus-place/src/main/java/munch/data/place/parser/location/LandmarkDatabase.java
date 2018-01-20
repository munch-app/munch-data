package munch.data.place.parser.location;

import munch.data.structure.Place;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 20/1/2018
 * Time: 3:41 PM
 * Project: munch-data
 */
@Singleton
public final class LandmarkDatabase {

    private final TrainDatabase trainDatabase;

    @Inject
    public LandmarkDatabase(TrainDatabase trainDatabase) {
        this.trainDatabase = trainDatabase;
    }

    /**
     * @param lat latitude
     * @param lng longitude
     * @return nearest landmark
     */
    public List<Place.Location.Landmark> find(double lat, double lng) {
        TrainStation nearest = trainDatabase.findNearest(lat, lng);

        Place.Location.Landmark landmark = new Place.Location.Landmark();
        landmark.setName(nearest.getName() + " MRT");
        landmark.setType("train");
        landmark.setLatLng(nearest.getLat() + "," + nearest.getLng());
        return List.of(landmark);
    }
}
