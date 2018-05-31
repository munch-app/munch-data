package munch.data.location;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import munch.data.VersionedObject;

import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 30/5/18
 * Time: 2:07 PM
 * Project: munch-data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Landmark implements VersionedObject {
    private String landmarkId;

    private String name;
    private String type;
    private String latLng;

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

    public String getLatLng() {
        return latLng;
    }

    public void setLatLng(String latLng) {
        this.latLng = latLng;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Landmark landmark = (Landmark) o;
        return Objects.equals(name, landmark.name) &&
                Objects.equals(type, landmark.type) &&
                Objects.equals(latLng, landmark.latLng);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, latLng);
    }

    @Override
    public String toString() {
        return "Landmark{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", latLng='" + latLng + '\'' +
                '}';
    }

    @Override
    public String getVersion() {
        return "2018-05-30";
    }
}
