package munch.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by: Fuxing
 * Date: 30/5/18
 * Time: 2:11 PM
 * Project: munch-data
 */
public interface ElasticObject {

    @JsonProperty("dataType")
    String getDataType();
}
