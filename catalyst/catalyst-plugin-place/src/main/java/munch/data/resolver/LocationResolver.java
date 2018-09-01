package munch.data.resolver;

import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import catalyst.source.SourceMappingCache;
import com.google.common.base.Joiner;
import edit.utils.PatternSplit;
import munch.data.location.City;
import munch.data.location.Country;
import munch.data.location.Location;
import munch.location.CountryCity;
import munch.location.LocationClient;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 11/8/18
 * Time: 7:01 AM
 * Project: munch-data
 */
@Singleton
public final class LocationResolver {
    private static final Pattern NUMBER_PATTERN = Pattern.compile(".*\\d+.*");
    private static final PatternSplit DIVIDER_PATTERN = PatternSplit.compile("(?<!-) (?!-)");
    private static final PatternSplit ADDRESS_DIVIDER_PATTERN = PatternSplit.compile("([^a-z'’]|^|[^a-z]'’)[a-z]");
    private static final Set<String> BLOCKED_UNIT_NUMBERS = Set.of("#-", "#", "-", "-#");

    private static final Pattern UNIT_PATTERN = Pattern.compile(".*[0-9]+-[0-9]+.*");

    private static final PatternSplit COMMA_PATTERN = PatternSplit.compile(", *");
    private static final Pattern COMMA_SEQ_PATTERN = Pattern.compile(", *,");
    private static final Pattern COMMA_PRE_POST_PATTERN = Pattern.compile("^,|,$");

    private final SourceMappingCache sourceMappingCache;
    private final LandmarkResolver landmarkResolver;
    private final LocationClient locationClient;

    @Inject
    public LocationResolver(SourceMappingCache sourceMappingCache, LandmarkResolver landmarkResolver, LocationClient locationClient) {
        this.sourceMappingCache = sourceMappingCache;
        this.landmarkResolver = landmarkResolver;
        this.locationClient = locationClient;
    }

    public Location resolve(PlaceMutation mutation) throws LocationSupportException {
        String latLng = getFirst(mutation.getLatLng());
        Objects.requireNonNull(latLng);

        String[] split = latLng.split(",");
        double lat = Double.parseDouble(split[0].trim());
        double lng = Double.parseDouble(split[1].trim());

        Location location = new Location();
        location.setUnitNumber(getFirst(mutation.getUnitNumber()));
        location.setNeighbourhood(locationClient.getNeighbourhood(lat, lng));

        location.setStreet(getFirst(mutation.getStreet()));
        if (StringUtils.isBlank(location.getStreet())) location.setStreet(locationClient.getStreet(lat, lng));

        CountryCity countryCity = locationClient.getCity(lat, lng);
        if (countryCity == null) throw new LocationSupportException();
        switch (countryCity.getCity().toLowerCase()) {
            case "singapore":
                location.setCity(City.singapore);
                location.setCountry(Country.SGP);
                break;
            default:
                throw new LocationSupportException();
        }

        location.setPostcode(getFirst(mutation.getPostcode()));
        location.setLatLng(latLng);
        location.setLandmarks(landmarkResolver.resolve(latLng));
        location.setAddress(getAddress(mutation, location));
        return location;
    }

    @Nullable
    public String getFirst(List<MutationField<String>> fields) {
        if (fields.isEmpty()) return null;
        return StringUtils.trimToNull(fields.get(0).getValue());
    }

    private String getAddress(PlaceMutation mutation, Location location) {
        List<MutationField<String>> addresses = mutation.getAddress();
        // Get Form Address
        for (MutationField<String> address : addresses) {
            for (MutationField.Source source : address.getSources()) {
                if (sourceMappingCache.isForm(source.getSource())) return address.getValue();
            }
        }

        // If address don't exist, create one
        if (addresses.isEmpty()) {
            return Joiner.on(", ")
                    .skipNulls()
                    .join(location.getStreet(), location.getUnitNumber(),
                            location.getCity() + " " + location.getPostcode());
        }

        String address = Objects.requireNonNull(getFirst(mutation.getAddress()));
        if (hasUnitNumber(address) && address.contains(location.getPostcode())) return formatAddress(address);
        if (StringUtils.isBlank(location.getUnitNumber())) return formatAddress(address);

        // Else find any that contains - & # and return it (UnitNumber), else return max address
        return mutation.getAddress().stream()
                .filter(field -> hasUnitNumber(field.getValue()))
                .findAny()
                .map(field -> formatAddress(field.getValue()))
                .orElseGet(() -> {
                    if (StringUtils.isBlank(location.getUnitNumber())) return formatAddress(address);

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

                    return formatAddress(address);
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
}
