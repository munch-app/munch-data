package munch.data.place.parser.location;

import catalyst.utils.LatLngUtils;

import javax.annotation.Nullable;
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

    @Nullable
    @Override
    public LatLngUtils.LatLng geocode(String postal) {
        // TODO wait for outstanding orders
        // http://www.geopostcodes.com/account
        return null;
    }
}
