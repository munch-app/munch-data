package munch.data.elastic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import munch.data.location.Country;

import java.util.Set;

/**
 * Implement this if the ElasticObject must implement suggest object
 * <p>
 * Created by: Fuxing
 * Date: 1/6/18
 * Time: 3:19 PM
 * Project: munch-data
 */
public interface SuggestObject extends ElasticObject {

    @JsonProperty("name")
    String getName();

    /**
     * @return suggest object that apply in the country, you can supply as many related objects as possible
     */
    @JsonIgnore
    Context getSuggestContext();

    interface Context {
        Set<Country> getCountries();

        /**
         * @return Implement if latLng is available
         */
        default String getLatLng() {
            return null;
        }

        /**
         * @return Implement if weight is available
         */
        default int getWeight() {
            return 1;
        }
    }

    interface Names {

        /**
         * @return alternative names that apply for suggest
         */
        @JsonProperty("names")
        Set<String> getNames();
    }
}
