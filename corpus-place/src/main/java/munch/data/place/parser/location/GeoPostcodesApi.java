package munch.data.place.parser.location;

import catalyst.utils.LatLngUtils;
import corpus.location.GeocodeClient;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * http://www.geopostcodes.com
 * <p>
 * Created by: Fuxing
 * Date: 20/11/2017
 * Time: 5:13 PM
 * Project: munch-data
 */
@Singleton
public final class GeoPostcodesApi implements GeocodeApi {

    private final GeocodeClient geocodeClient;

    @Inject
    public GeoPostcodesApi(GeocodeClient geocodeClient) {
        this.geocodeClient = geocodeClient;
    }

    /**
     * Based on data from http://www.geopostcodes.com
     *
     * @param postal postal code geocode
     * @return Nullable or Geocode LatLng
     */
    @Nullable
    @Override
    public LatLngUtils.LatLng geocode(String postal) {
        GeocodeClient.Data data = geocodeClient.geocodePostcode(postal);
        if (data == null) return null;

        return new LatLngUtils.LatLng(data.getLat(), data.getLng());
    }
}
