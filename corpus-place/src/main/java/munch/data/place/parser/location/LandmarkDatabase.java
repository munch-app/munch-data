package munch.data.place.parser.location;

import catalyst.utils.LatLngUtils;
import corpus.data.CorpusClient;
import corpus.field.FieldUtils;
import munch.data.structure.Place;
import munch.data.utils.ScheduledThreadUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by: Fuxing
 * Date: 20/1/2018
 * Time: 3:41 PM
 * Project: munch-data
 */
@Singleton
public final class LandmarkDatabase {
    private static final Logger logger = LoggerFactory.getLogger(LandmarkDatabase.class);

    private final CorpusClient corpusClient;
    private List<Landmark> landmarks = new ArrayList<>();

    @Inject
    public LandmarkDatabase(CorpusClient corpusClient) {
        this.corpusClient = corpusClient;
        sync();
        ScheduledThreadUtils.schedule(this::sync, 24, TimeUnit.HOURS);
    }

    /**
     * @param lat latitude
     * @param lng longitude
     * @return nearest landmark
     */
    public List<Place.Location.Landmark> find(double lat, double lng) {
        Landmark nearest = findNearest(lat, lng, "train");

        Place.Location.Landmark landmark = new Place.Location.Landmark();
        landmark.setName(nearest.name);
        landmark.setType(nearest.type);
        landmark.setLatLng(nearest.latLng);
        return List.of(landmark);
    }

    /**
     * @param lat latitude of place
     * @param lng longitude of place
     * @return Nearest MRTStation
     */
    private Landmark findNearest(double lat, double lng, String type) {
        double shortest = Double.MAX_VALUE;
        Landmark nearest = null;

        // Iterate and replace to get shortest distance
        for (Landmark station : landmarks) {
            if (station.type.equals(type)) {
                double distance = distance(lat, lng, station);
                if (distance < shortest) {
                    shortest = distance;
                    nearest = station;
                }
            }
        }
        return nearest;
    }

    private void sync() {
        List<Landmark> landmarks = new ArrayList<>();
        corpusClient.list("Sg.Munch.Location.Landmark").forEachRemaining(data -> {
            String name = FieldUtils.getValue(data, "Landmark.name");
            String type = FieldUtils.getValue(data, "Landmark.type");
            String latLngString = FieldUtils.getValue(data, "Landmark.latLng");
            if (StringUtils.isAnyBlank(name, type, latLngString)) return;

            try {
                LatLngUtils.LatLng latLng = LatLngUtils.parse(latLngString);
                if (latLng == null) return;

                Landmark landmark = new Landmark();
                landmark.name = name;
                landmark.type = type;
                landmark.latLng = latLngString;

                landmark.lat = latLng.getLat();
                landmark.lng = latLng.getLng();
                landmarks.add(landmark);
            } catch (LatLngUtils.ParseException e) {
                logger.info("Failed to parse latLng", e);
            }
        });

        this.landmarks = landmarks;
    }

    public static double distance(double lat, double lng, Landmark station) {
        return distance(lat, station.lat, lng, station.lng, 0, 0);
    }

    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     * <p>
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     *
     * @return Distance in Meters
     */
    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {
        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    public class Landmark {
        private String name;
        private String type;
        private String latLng;

        private double lat;
        private double lng;
    }
}
