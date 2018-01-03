package munch.data.place.parser.location;

import catalyst.utils.LatLngUtils;
import com.google.common.base.Joiner;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import corpus.location.LocationClient;
import munch.data.place.matcher.PatternSplit;
import munch.data.place.parser.AbstractParser;
import munch.data.structure.Place;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 10:12 PM
 * Project: munch-data
 */
@Singleton
public final class LocationParser extends AbstractParser<Place.Location> {
    private static final Pattern NUMBER_PATTERN = Pattern.compile(".*\\d+.*");
    private static final PatternSplit DIVIDER_PATTERN = PatternSplit.compile("(?<!-) (?!-)");

    private final TrainDatabase trainDatabase; // With latLng
    private final LocationClient locationClient;

    @Inject
    public LocationParser(TrainDatabase trainDatabase, LocationClient locationClient) {
        this.trainDatabase = trainDatabase;
        this.locationClient = locationClient;
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
        location.setNearestTrain(trainDatabase.findNearest(lat, lng).getName());
        location.setStreet(collectStreet(lat, lng));
        location.setBuilding(collectMax(list, WordUtils::capitalizeFully, PlaceKey.Location.building));
        location.setUnitNumber(collectUnitNumber(list));

        location.setCity(collectMax(list, WordUtils::capitalizeFully, PlaceKey.Location.city));
        location.setCountry(collectMax(list, WordUtils::capitalizeFully, PlaceKey.Location.country));

        // Address can be constructed from other parts
        location.setAddress(collectAddress(location, list));

        location.setPostal(collectMax(list, PlaceKey.Location.postal));
        location.setLatLng(lat, lng);
        return location;
    }

    /**
     * @param lat latitude
     * @param lng longitude
     * @return street name or Singapore if null
     */
    private String collectStreet(double lat, double lng) {
        String street = locationClient.street(lat, lng);
        if (StringUtils.isNotBlank(street)) return street;
        return "Singapore";
    }

    /**
     * @param list list of cd
     * @return find unit number, if cannot be found, try get from address
     */
    private String collectUnitNumber(List<CorpusData> list) {
        String unitNumber = collectMax(list, PlaceKey.Location.unitNumber);
        if (StringUtils.isNotBlank(unitNumber)) {
            return cleanUnitNumber(unitNumber);
        }

        List<String> unitNumbers = collect(list, PlaceKey.Location.address).stream()
                .map(CorpusData.Field::getValue)
                .map(this::parseUnitNumber)
                .map(this::cleanUnitNumber)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return collectMax(unitNumbers);
    }

    @Nullable
    public String parseUnitNumber(String text) {
        if (StringUtils.isBlank(text)) return null;

        for (String part : DIVIDER_PATTERN.split(text)) {
            if (part.contains("-") && part.contains("#") && NUMBER_PATTERN.matcher(part).matches()) {
                return part;
            }
        }
        return null;
    }

    public String cleanUnitNumber(String unitNumber) {
        if (unitNumber == null) return null;

        unitNumber = unitNumber.replaceAll(" ", "");
        unitNumber = unitNumber.replaceAll("^,|,$", "");
        if (unitNumber.startsWith("#")) return unitNumber;
        if (unitNumber.isEmpty()) return null;
        return "#" + unitNumber.toUpperCase();
    }

    private String collectAddress(Place.Location location, List<CorpusData> list) {
        String address = collectMax(list, WordUtils::capitalize, PlaceKey.Location.address);
        if (address == null) {
            // If address don't exist, create one
            return Joiner.on(", ")
                    .skipNulls()
                    .join(location.getUnitNumber(), location.getStreet(),
                            location.getCity() + " " + location.getPostal());
        }
        // If max contains - & # return it
        if (address.contains("-") && address.contains("#")) return address;

        // Else find any that contains - & # and return it, else return max address
        return collect(list, PlaceKey.Location.address).stream()
                .map(CorpusData.Field::getValue)
                .filter(text -> text.contains("-") && text.contains("#"))
                .findAny()
                .map(WordUtils::capitalizeFully)
                .orElse(address);
    }

    /**
     * @param list list of corpus data
     * @return find LatLng
     */
    private LatLngUtils.LatLng parseLatLng(List<CorpusData> list) {
        String latLng = collectMax(list, PlaceKey.Location.latLng);
        String postal = collectMax(list, PlaceKey.Location.postal);
        if (StringUtils.isBlank(postal)) return null;

        LocationClient.Data geocode = locationClient.geocodePostcode(postal);
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
