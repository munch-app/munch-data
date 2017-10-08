package munch.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by: Fuxing
 * Date: 27/8/2017
 * Time: 4:49 PM
 * Project: munch-core
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tag implements SearchResult {
    private String id;

    private String name;
    private String type;

    /**
     * For Tag, id is not very important since tag should be identified by name instead
     *
     * @return id of the tag, is a SHA256 value quite long
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getDataType() {
        return "Tag";
    }
}
