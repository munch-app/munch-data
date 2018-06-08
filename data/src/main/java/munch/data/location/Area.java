package munch.data.location;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import munch.data.*;
import munch.file.Image;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
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

    private Counts counts;
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

    public Counts getCounts() {
        return counts;
    }

    public void setCounts(Counts counts) {
        this.counts = counts;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Area area = (Area) o;
        return Objects.equals(areaId, area.areaId) &&
                type == area.type &&
                Objects.equals(name, area.name) &&
                Objects.equals(names, area.names) &&
                Objects.equals(website, area.website) &&
                Objects.equals(description, area.description) &&
                Objects.equals(images, area.images) &&
                Objects.equals(hours, area.hours) &&
                Objects.equals(location, area.location) &&
                Objects.equals(locationCondition, area.locationCondition) &&
                Objects.equals(counts, area.counts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(areaId, type, name, names, website, description, images, hours, location, locationCondition, counts);
    }

    @Override
    public String toString() {
        return "Area{" +
                "areaId='" + areaId + '\'' +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", names=" + names +
                ", website='" + website + '\'' +
                ", description='" + description + '\'' +
                ", images=" + images +
                ", hours=" + hours +
                ", location=" + location +
                ", locationCondition=" + locationCondition +
                ", counts=" + counts +
                ", createdMillis=" + createdMillis +
                ", updatedMillis=" + updatedMillis +
                '}';
    }

    /**
     * Location condition to confirm place is actually inside a cluster beyond polygon matching
     */
    public static class LocationCondition {
        private Set<String> postcodes;
        private Set<String> unitNumbers;

        public Set<String> getPostcodes() {
            return postcodes;
        }

        public void setPostcodes(Set<String> postcodes) {
            this.postcodes = postcodes;
        }

        public Set<String> getUnitNumbers() {
            return unitNumbers;
        }

        public void setUnitNumbers(Set<String> unitNumbers) {
            this.unitNumbers = unitNumbers;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LocationCondition that = (LocationCondition) o;
            return Objects.equals(postcodes, that.postcodes) &&
                    Objects.equals(unitNumbers, that.unitNumbers);
        }

        @Override
        public int hashCode() {

            return Objects.hash(postcodes, unitNumbers);
        }
    }

    public static class Counts {
        private long total;

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Counts counts = (Counts) o;
            return total == counts.total;
        }

        @Override
        public int hashCode() {
            return Objects.hash(total);
        }

        @Override
        public String toString() {
            return "Counts{" +
                    "total=" + total +
                    '}';
        }
    }

    public enum Type {
        @JsonProperty("Region")
        Region,
        @JsonProperty("Cluster")
        Cluster
    }
}
