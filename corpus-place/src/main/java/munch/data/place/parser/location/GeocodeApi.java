package munch.data.place.parser.location;

import catalyst.utils.LatLngUtils;

import javax.annotation.Nullable;

/**
 * Created by: Fuxing
 * Date: 20/11/2017
 * Time: 5:07 PM
 * Project: munch-data
 */
public interface GeocodeApi {

    /**
     * @param postal postal code geocode
     * @return LatLng, might be nullable
     */
    @Nullable
    LatLngUtils.LatLng geocode(String postal);

    class Chain implements GeocodeApi {
        private GeocodeApi[] geocoders;

        /**
         * @param geocoders list of geocoders to chain together as fallback
         */
        public Chain(GeocodeApi... geocoders) {
            this.geocoders = geocoders;
        }

        @Nullable
        @Override
        public LatLngUtils.LatLng geocode(String postal) {
            for (GeocodeApi geocoder : geocoders) {
                LatLngUtils.LatLng latLng = geocoder.geocode(postal);
                if (latLng != null) return latLng;
            }
            return null;
        }
    }
}

