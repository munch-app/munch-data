package munch.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by: Fuxing
 * Date: 30/5/18
 * Time: 3:17 PM
 * Project: munch-data
 */
public interface VersionedObject {

    /**
     * @return version of object: e.g: 2018-05-05
     */
    @JsonProperty("version")
    String getVersion();
}
