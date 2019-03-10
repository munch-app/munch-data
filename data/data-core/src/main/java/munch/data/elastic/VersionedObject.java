package munch.data.elastic;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by: Fuxing
 * Date: 30/5/18
 * Time: 3:17 PM
 * Project: munch-data
 */
public interface VersionedObject {

    /**
     * 2018-05-05
     * Started versioned object
     * <p>
     * 2019-03-10
     * Update elastic index for scale and fixes low level problem
     *
     * @return version of object: e.g: 2018-05-05
     */
    @JsonProperty("version")
    String getVersion();
}
