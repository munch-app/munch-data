package munch.data.tag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import munch.data.ElasticObject;
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
public final class Tag implements ElasticObject, VersionedObject {
    private String tagId;

    private String type;
    private String name;

    private Set<String> names;
    private long createdMillis;
    private long updatedMillis;

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
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

    public Set<String> getNames() {
        return names;
    }

    public void setNames(Set<String> names) {
        this.names = names;
    }

    public long getCreatedMillis() {
        return createdMillis;
    }

    public void setCreatedMillis(long createdMillis) {
        this.createdMillis = createdMillis;
    }

    public long getUpdatedMillis() {
        return updatedMillis;
    }

    public void setUpdatedMillis(long updatedMillis) {
        this.updatedMillis = updatedMillis;
    }

    @Override
    public String getDataType() {
        return "Tag";
    }

    @Override
    public String getVersion() {
        return "2018-05-30";
    }
}
