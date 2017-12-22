package munch.data.place.parser.location;

import catalyst.utils.LatLngUtils;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import corpus.location.GeocodeClient;
import munch.data.place.matcher.PatternSplit;
import munch.data.place.parser.AbstractParser;
import munch.data.structure.Place;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 10:12 PM
 * Project: munch-data
 */
@Singleton
public final class LocationParser extends AbstractParser<Place.Location> {
    private static final Pattern NUMBER_PATTERN = Pattern.compile(".*\\d+.*");
    private static final PatternSplit DIVIDER_PATTERN = PatternSplit.compile(" ");

    private final TrainDatabase trainDatabase; // With latLng
    private final StreetNameClient streetNameClient; // With latLng
    private final GeocodeClient geocodeClient;

    @Inject
    public LocationParser(TrainDatabase trainDatabase, StreetNameClient streetNameClient, GeocodeClient geocodeClient) {
        this.trainDatabase = trainDatabase;
        this.streetNameClient = streetNameClient;
        this.geocodeClient = geocodeClient;
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
        location.setBuilding(collectMax(list, WordUtils::capitalizeFully, PlaceKey.Location.building));
        location.setNearestTrain(trainDatabase.findNearest(lat, lng).getName());
        location.setUnitNumber(collectUnitNumber(location, list));

        location.setCity(collectMax(list, WordUtils::capitalizeFully, PlaceKey.Location.city));
        location.setCountry(collectMax(list, WordUtils::capitalizeFully, PlaceKey.Location.country));

        location.setPostal(collectMax(list, PlaceKey.Location.postal));
        location.setLatLng(lat, lng);
        return location;
    }

    /**
     * @param location location
     * @param list     list of cd
     * @return find unit number, if cannot be found, try get from address
     */
    private String collectUnitNumber(Place.Location location, List<CorpusData> list) {
        String unitNumber = collectMax(list, PlaceKey.Location.unitNumber);
        if (StringUtils.isNotBlank(unitNumber)) return unitNumber;

        String address = location.getAddress();
        if (StringUtils.isBlank(address)) return null;
        return parseUnitNumber(address);
    }

    @Nullable
    public String parseUnitNumber(String text) {
        if (StringUtils.isBlank(text)) return null;

        List<String> split = DIVIDER_PATTERN.split(text);
        for (String part : split) {
            if (part.contains("-") && part.contains("#") && NUMBER_PATTERN.matcher(part).matches()) {
                return part;
            }
        }
        return null;
    }

    /**
     * @param list list of corpus data
     * @return find LatLng
     */
    private LatLngUtils.LatLng parseLatLng(List<CorpusData> list) {
        String latLng = collectMax(list, PlaceKey.Location.latLng);
        String postal = collectMax(list, PlaceKey.Location.postal);
        if (StringUtils.isBlank(postal)) return null;

        GeocodeClient.Data geocode = geocodeClient.geocodePostcode(postal);
        LatLngUtils.LatLng existing = LatLngUtils.parse(latLng);
        if (geocode == null) return existing;

        // If existing exist use existing if < 500
        if (existing != null && existing.distance(geocode.getLat(), geocode.getLng()) < 500) {
            // Never simply use given existing LatLng, you cannot trust them
            return existing;
        }
        return new LatLngUtils.LatLng(geocode.getLat(), geocode.getLng());
    }
}
