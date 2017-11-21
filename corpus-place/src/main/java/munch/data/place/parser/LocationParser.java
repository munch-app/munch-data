package munch.data.place.parser;

import catalyst.utils.LatLngUtils;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.place.parser.location.GeocodeApi;
import munch.data.place.parser.location.StreetNameClient;
import munch.data.place.parser.location.TrainDatabase;
import munch.data.structure.Place;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 10:12 PM
 * Project: munch-data
 */
@Singleton
public final class LocationParser extends AbstractParser<Place.Location> {

    private final TrainDatabase trainDatabase; // With latLng
    private final StreetNameClient streetNameClient; // With latLng
    private final GeocodeApi geocodeApi;

    @Inject
    public LocationParser(TrainDatabase trainDatabase, StreetNameClient streetNameClient, GeocodeApi geocodeApi) {
        this.trainDatabase = trainDatabase;
        this.streetNameClient = streetNameClient;
        this.geocodeApi = geocodeApi;
    }

    @Override
    @Nullable
    public Place.Location parse(Place place, List<CorpusData> list) {
        LatLngUtils.LatLng latLng = parseLatLng(list);
        if (latLng == null) return null;

        double lat = latLng.getLat();
        double lng = latLng.getLng();

        Place.Location location = new Place.Location();
        // Might Need to be smarter
        location.setStreet(streetNameClient.getStreet(lat, lng));
        location.setAddress(collectMax(list, WordUtils::capitalizeFully, PlaceKey.Location.address));
        location.setUnitNumber(collectMax(list, PlaceKey.Location.unitNumber));
        location.setBuilding(collectMax(list, WordUtils::capitalizeFully, PlaceKey.Location.building));
        location.setNearestTrain(trainDatabase.findNearest(lat, lng).getName());

        location.setCity(collectMax(list, WordUtils::capitalizeFully, PlaceKey.Location.city));
        location.setCountry(collectMax(list, WordUtils::capitalizeFully, PlaceKey.Location.country));

        location.setPostal(collectMax(list, PlaceKey.Location.postal));
        location.setLatLng(lat, lng);
        return location;
    }

    /**
     * @param list list of corpus data
     * @return find LatLng
     */
    private LatLngUtils.LatLng parseLatLng(List<CorpusData> list) {
        String latLng = collectMax(list, PlaceKey.Location.latLng);
        String postal = collectMax(list, PlaceKey.Location.postal);
        if (StringUtils.isBlank(postal)) return null;

        LatLngUtils.LatLng geocode = geocodeApi.geocode(postal);
        LatLngUtils.LatLng existing = LatLngUtils.parse(latLng);
        if (geocode == null) return existing;

        // If existing exist use existing if < 500
        if (existing != null && existing.distance(geocode) < 500) {
            // Never simply use given existing LatLng, you cannot trust them
            return existing;
        }
        return geocode;
    }
}
