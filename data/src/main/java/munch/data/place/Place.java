package munch.data.place;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import munch.data.ElasticObject;
import munch.data.VersionedObject;

/**
 * Created by: Fuxing
 * Date: 30/5/18
 * Time: 2:07 PM
 * Project: munch-data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Place implements ElasticObject, VersionedObject {
    private String placeId;

    @Override
    public String getDataType() {
        return "Place";
    }

    @Override
    public String getVersion() {
        return "2018-05-30";
    }
}
