package munch.data.resolver;

import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import munch.data.Location;
import munch.location.CountryCity;
import munch.location.LocationClient;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 11/8/18
 * Time: 7:01 AM
 * Project: munch-data
 */
@Singleton
public final class LocationResolver {

    private final LandmarkResolver landmarkResolver;
    private final LocationClient locationClient;

    @Inject
    public LocationResolver(LandmarkResolver landmarkResolver, LocationClient locationClient) {
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

        location.setAddress(getFirst(mutation.getAddress()));
        location.setUnitNumber(getFirst(mutation.getUnitNumber()));
        location.setNeighbourhood(locationClient.getNeighbourhood(lat, lng));

        location.setStreet(getFirst(mutation.getStreet()));
        if (StringUtils.isBlank(location.getStreet())) location.setStreet(locationClient.getStreet(lat, lng));

        CountryCity countryCity = locationClient.getCity(lat, lng);
        if (countryCity == null) throw new LocationSupportException();
        location.setCity(countryCity.getCity());
        location.setCountry(countryCity.getCountry());

        location.setPostcode(getFirst(mutation.getPostcode()));
        location.setLatLng(latLng);
        location.setLandmarks(landmarkResolver.resolve(latLng));
        return location;
    }

    @Nullable
    public String getFirst(List<MutationField<String>> fields) {
        if (fields.isEmpty()) return null;
        return StringUtils.trimToNull(fields.get(0).getValue());
    }
}
