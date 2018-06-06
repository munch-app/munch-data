package munch.data.location;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import munch.data.*;
import munch.file.Image;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 30/5/18
 * Time: 2:07 PM
 * Project: munch-data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Area implements ElasticObject, VersionedObject, SuggestObject, CorrectableObject {
    private String areaId;

    private Type type;
    private String name;
    private Set<String> names;

    private String website;
    private String description;
    private List<Image> images;
    private List<Hour> hours;

    private Location location;
    private LocationCondition locationCondition;

    private long createdMillis;
    private long updatedMillis;

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    @NotNull
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    @NotBlank
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull
    public Set<String> getNames() {
        return names;
    }

    public void setNames(Set<String> names) {
        this.names = names;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NotNull
    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    @NotNull
    public List<Hour> getHours() {
        return hours;
    }

    public void setHours(List<Hour> hours) {
        this.hours = hours;
    }

    @NotNull
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public LocationCondition getLocationCondition() {
        return locationCondition;
    }

    public void setLocationCondition(LocationCondition locationCondition) {
        this.locationCondition = locationCondition;
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
        return "Area";
    }

    @Override
    public String getDataId() {
        return areaId;
    }

    @Override
    public String getVersion() {
        return "2018-05-30";
    }

    /**
     * Location condition to confirm place is actually inside a cluster beyond polygon matching
     */
    public static class LocationCondition {
        private Set<String> postcodes;
        private Set<String> unitNumbers;

        @NotNull
        public Set<String> getPostcodes() {
            return postcodes;
        }

        public void setPostcodes(Set<String> postcodes) {
            this.postcodes = postcodes;
        }

        @NotNull
        public Set<String> getUnitNumbers() {
            return unitNumbers;
        }

        public void setUnitNumbers(Set<String> unitNumbers) {
            this.unitNumbers = unitNumbers;
        }


    }

    public enum Type {
        @JsonProperty("Region")
        Region,
        @JsonProperty("Cluster")
        Cluster
    }
}
