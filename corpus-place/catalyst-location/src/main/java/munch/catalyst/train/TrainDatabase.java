package munch.catalyst.train;

import catalyst.utils.LatLngUtils;
import com.typesafe.config.Config;
import munch.corpus.docs.GoogleSheet;
import munch.corpus.docs.GoogleSheetIterator;
import munch.corpus.docs.SheetNotFound;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 28/7/2017
 * Time: 3:06 PM
 * Project: munch-corpus
 */

@Singleton
public final class TrainDatabase {
    private static final Logger logger = LoggerFactory.getLogger(TrainDatabase.class);

    private final String docId;
    private final String indexId;

    private List<TrainStation> stations = new ArrayList<>();

    @Inject
    public TrainDatabase(Config config) {
        this.docId = config.getString("train.sheet.docId");
        this.indexId = config.getString("train.sheet.indexId");
    }

    public void sync() throws IOException, SheetNotFound {
        GoogleSheet googleSheet = new GoogleSheet(docId, indexId);
        GoogleSheetIterator iterator = new GoogleSheetIterator(googleSheet);

        List<TrainStation> stations = new ArrayList<>();
        while (iterator.hasNext()) {
            TrainStation station = parseStation(iterator);
            if (station != null) stations.add(station);
        }

        this.stations = stations;
    }

    private static TrainStation parseStation(GoogleSheetIterator iterator) {
        iterator.next();

        String name = iterator.get("NAME");
        String latLngString = iterator.get("LAT_LNG");
        if (StringUtils.isAnyBlank(name, latLngString)) return null;

        try {
            LatLngUtils.LatLng latLng = LatLngUtils.parse(latLngString);

            TrainStation station = new TrainStation();
            station.setName(name);
            station.setLat(latLng.getLat());
            station.setLng(latLng.getLng());
            return station;
        } catch (LatLngUtils.ParseException e) {
            logger.info("Failed to parse latLng", e);
            return null;
        }
    }

    /**
     * @param lat latitude of place
     * @param lng longitude of place
     * @return Nearest MRTStation
     */
    public TrainStation findNearest(double lat, double lng) {
        double shortest = Double.MAX_VALUE;
        TrainStation nearest = null;

        // Iterate and replace to get shortest distance
        for (TrainStation station : stations) {
            double distance = distance(lat, lng, station);
            if (distance < shortest) {
                shortest = distance;
                nearest = station;
            }
        }
        return nearest;
    }

    public TrainStation findNearest(String latLng) throws LatLngUtils.ParseException {
        LatLngUtils.LatLng parsed = LatLngUtils.parse(latLng);
        return findNearest(parsed.getLat(), parsed.getLng());
    }

    public static double distance(double lat, double lng, TrainStation station) {
        return distance(lat, station.getLat(), lng, station.getLng(), 0, 0);
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
}
