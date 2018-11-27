package munch.data.location;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import munch.data.ElasticObject;
import munch.data.VersionedObject;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 30/5/18
 * Time: 2:07 PM
 * Project: munch-data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Landmark implements ElasticObject, VersionedObject {
    private String landmarkId;

    private Type type;
    private String name;
    private Location location;

    private Long updatedMillis;
    private Long createdMillis;

    public String getLandmarkId() {
        return landmarkId;
    }

    public void setLandmarkId(String landmarkId) {
        this.landmarkId = landmarkId;
    }

    @NotBlank
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @NotNull
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @NotNull
    @Override
    public Long getUpdatedMillis() {
        return updatedMillis;
    }

    public void setUpdatedMillis(Long updatedMillis) {
        this.updatedMillis = updatedMillis;
    }

    @NotNull
    @Override
    public Long getCreatedMillis() {
        return createdMillis;
    }

    public void setCreatedMillis(Long createdMillis) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Landmark landmark = (Landmark) o;
        return Objects.equals(landmarkId, landmark.landmarkId) &&
                type == landmark.type &&
                Objects.equals(name, landmark.name) &&
                Objects.equals(location, landmark.location);
    }

    @Override
    public int hashCode() {

        return Objects.hash(landmarkId, type, name, location);
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

    public enum Type {
        @JsonProperty("train")
        train
    }
}
