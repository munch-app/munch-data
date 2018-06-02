package munch.data.location;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import munch.data.ElasticObject;
import munch.data.Location;
import munch.data.SuggestObject;
import munch.data.VersionedObject;

/**
 * Created by: Fuxing
 * Date: 30/5/18
 * Time: 2:07 PM
 * Project: munch-data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Landmark implements VersionedObject, ElasticObject, SuggestObject {
    private String landmarkId;

    private String type;
    private String name;
    private Location location;

    private long updatedMillis;
    private long createdMillis;

    public String getLandmarkId() {
        return landmarkId;
    }

    public void setLandmarkId(String landmarkId) {
        this.landmarkId = landmarkId;
    }

    @Override
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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public long getUpdatedMillis() {
        return updatedMillis;
    }

    public void setUpdatedMillis(long updatedMillis) {
        this.updatedMillis = updatedMillis;
    }

    public long getCreatedMillis() {
        return createdMillis;
    }

    public void setCreatedMillis(long createdMillis) {
        this.createdMillis = createdMillis;
    }

    @Override
    public String toString() {
        return "Landmark{" +
                "landmarkId='" + landmarkId + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", location=" + location +
                ", updatedMillis=" + updatedMillis +
                ", createdMillis=" + createdMillis +
                '}';
    }

    @Override
    public String getVersion() {
        return "2018-05-30";
    }

    @Override
    public String getDataType() {
        return "Landmark";
    }

    @Override
    public String getDataId() {
        return landmarkId;
    }
}
