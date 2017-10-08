package munch.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by: Fuxing
 * Date: 11/7/2017
 * Time: 11:51 AM
 * Project: munch-core
 */
public interface SearchResult {

    /**
     * @return type, Case Sensitive
     */
    @JsonProperty("dataType")
    String getDataType();

}
