package munch.data.place.parser.location;

import catalyst.utils.LatLngUtils;
import com.google.common.base.Joiner;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import corpus.location.LocationClient;
import munch.data.place.parser.AbstractParser;
import munch.data.structure.Place;
import munch.data.utils.PatternSplit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
    private static final PatternSplit DIVIDER_PATTERN = PatternSplit.compile("(?<!-) (?!-)");
    private static final PatternSplit ADDRESS_DIVIDER_PATTERN = PatternSplit.compile("([^a-z'’]|^|[^a-z]'’)[a-z]");
    private static final Set<String> BLOCKED_UNIT_NUMBERS = Set.of("#-", "#", "-", "-#");

    private static final Pattern UNIT_PATTERN = Pattern.compile(".*[0-9]+-[0-9]+.*");

    private static final PatternSplit COMMA_PATTERN = PatternSplit.compile(", *");
    private static final Pattern COMMA_SEQ_PATTERN = Pattern.compile(", *,");
    private static final Pattern COMMA_PRE_POST_PATTERN = Pattern.compile("^,|,$");

    private final LandmarkDatabase landmarkDatabase;
    private final LocationClient locationClient;
    private final LocationDatabase locationDatabase;

    @Inject
    public LocationParser(LandmarkDatabase landmarkDatabase, LocationClient locationClient, LocationDatabase locationDatabase) {
        this.landmarkDatabase = landmarkDatabase;
        this.locationClient = locationClient;
        this.locationDatabase = locationDatabase;
    }

    @Override
    @Nullable
    public Place.Location parse(Place place, List<CorpusData> list) {
        LatLngUtils.LatLng latLng = parseLatLng(list);
        if (latLng == null) return null;

        double lat = latLng.getLat();
        double lng = latLng.getLng();

        Place.Location location = new Place.Location();
        location.setLandmarks(landmarkDatabase.find(lat, lng));
        location.setStreet(collectStreet(lat, lng));
        location.setUnitNumber(collectUnitNumber(list));

        location.setNeighbourhood(collectNeighbourhood(lat, lng));
        location.setCity(collectMax(list, WordUtils::capitalizeFully, PlaceKey.Location.city));
        location.setCountry(collectMax(list, WordUtils::capitalizeFully, PlaceKey.Location.country));

        location.setPostal(collectMax(list, PlaceKey.Location.postal));
        location.setLatLng(lat, lng);

        // Address can be constructed from other parts
        String address = collectAddress(location, list);
        location.setAddress(formatAddress(address));
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
     * @param lat latitude
     * @param lng longitude
     * @return nearby neighbourhood or Singapore if null
     */
    private String collectNeighbourhood(double lat, double lng) {
        String location = locationDatabase.findLocation(lat, lng);
        if (StringUtils.isNotBlank(location)) return location;
        String neighbourhood = locationClient.neighbourhood(lat, lng);
        if (StringUtils.isNotBlank(neighbourhood)) return WordUtils.capitalizeFully(neighbourhood);
        return "Singapore";
    }

    /**
     * @param list list of cd
     * @return find unit number, if cannot be found, try get from address
     */
    private String collectUnitNumber(List<CorpusData> list) {
        List<String> unitNumbers = new ArrayList<>();

        collect(list, PlaceKey.Location.unitNumber).stream()
                .map(CorpusData.Field::getValue)
                .map(this::cleanUnitNumber)
                .filter(Objects::nonNull)
                .forEach(unitNumbers::add);

        collect(list, PlaceKey.Location.address).stream()
                .map(CorpusData.Field::getValue)
                .map(this::parseUnitNumber)
                .map(this::cleanUnitNumber)
                .filter(Objects::nonNull)
                .forEach(unitNumbers::add);

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

        unitNumber = unitNumber.replace(" ", "");
        unitNumber = COMMA_PRE_POST_PATTERN.matcher(unitNumber).replaceAll("");
        if (unitNumber.startsWith("#")) return unitNumber;
        if (unitNumber.toLowerCase().startsWith("stall")) return unitNumber;
        if (unitNumber.isEmpty()) return null;
        if (BLOCKED_UNIT_NUMBERS.contains(unitNumber.toLowerCase())) return null;
        return "#" + unitNumber.toUpperCase();
    }

    private String collectAddress(Place.Location location, List<CorpusData> list) {
        String address = collectMax(list, PlaceKey.Location.address);
        if (StringUtils.isBlank(address)) {
            // If address don't exist, create one
            return Joiner.on(", ")
                    .skipNulls()
                    .join(location.getStreet(), location.getUnitNumber(),
                            location.getCity() + " " + location.getPostal());
        }
        // If max contains - & # return it (UnitNumber)
        if (hasUnitNumber(address) && address.contains(location.getPostal())) return address;
        if (StringUtils.isBlank(location.getUnitNumber())) return address;

        // Else find any that contains - & # and return it (UnitNumber), else return max address
        return collectValue(list, PlaceKey.Location.address).stream()
                .filter(LocationParser::hasUnitNumber)
                .findAny()
                .orElseGet(() -> {
                    // Still no unit number found
                    List<String> parts = COMMA_PATTERN.splitRemoved(address);
                    parts.removeIf(StringUtils::isBlank);
                    for (int i = parts.size() - 1; i >= 0; i--) {
                        if (parts.get(i).toLowerCase().startsWith("singapore")) {
                            parts.add(i, location.getUnitNumber());
                            return Joiner.on(", ")
                                    .skipNulls()
                                    .join(parts);
                        }
                    }

                    return address;
                });
    }

    static boolean hasUnitNumber(String address) {
        return UNIT_PATTERN.matcher(address).matches();
    }

    static String formatAddress(String address) {
        address = address.toLowerCase();
        address = address.replace("\n", ""); // Line Break is not allowed
        address = COMMA_SEQ_PATTERN.matcher(address).replaceAll(", ");
        List<Object> split = ADDRESS_DIVIDER_PATTERN.split(address, 0, String::toUpperCase);
        return Joiner.on("").join(split);
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
