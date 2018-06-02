package munch.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by: Fuxing
 * Date: 1/6/18
 * Time: 3:19 PM
 * Project: munch-data
 */
public interface SuggestObject extends ElasticObject {

    @JsonProperty("name")
    String getName();

}
