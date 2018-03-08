package munch.data.structure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Set;

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

    private String type;
    private String name;

    private Set<String> converts;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Not for external use
    @JsonIgnore
    public Set<String> getConverts() {
        return converts;
    }

    public void setConverts(Set<String> converts) {
        this.converts = converts;
    }

    @Override
    public String getDataType() {
        return "Tag";
    }
}
