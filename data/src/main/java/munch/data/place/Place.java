package munch.data.place;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import munch.data.CorrectableObject;
import munch.data.ElasticObject;
import munch.data.SuggestObject;
import munch.data.VersionedObject;

import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 30/5/18
 * Time: 2:07 PM
 * Project: munch-data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Place implements ElasticObject, VersionedObject, SuggestObject, CorrectableObject {
    private String placeId;

    private String name;
    private Set<String> names;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<String> getNames() {
        return names;
    }

    @Override
    public String getDataType() {
        return "Place";
    }

    @Override
    public String getVersion() {
        return "2018-05-30";
    }

    @Override
    public String getDataId() {
        return placeId;
    }
}
