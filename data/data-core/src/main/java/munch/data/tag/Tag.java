package munch.data.tag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import munch.data.MultipleNameObject;
import munch.data.ElasticObject;
import munch.data.SuggestObject;
import munch.data.VersionedObject;

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
public final class Tag implements ElasticObject, VersionedObject, SuggestObject, MultipleNameObject {
    private String tagId;

    private Type type;
    private String name;
    private Set<String> names;

    private Place place;
    private Search search;
    private Counts counts;

    private long createdMillis;
    private long updatedMillis;

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * name is the main name for the tag
     * names are all the synonyms for the tag
     *
     * @return similar names
     */
    public Set<String> getNames() {
        return names;
    }

    public void setNames(Set<String> names) {
        this.names = names;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public Search getSearch() {
        return search;
    }

    public void setSearch(Search search) {
        this.search = search;
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
        return "Tag";
    }

    @Override
    public String getDataId() {
        return tagId;
    }

    @Override
    public String getVersion() {
        return "2018-05-30";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(tagId, tag.tagId) &&
                type == tag.type &&
                Objects.equals(name, tag.name) &&
                Objects.equals(names, tag.names) &&
                Objects.equals(place, tag.place) &&
                Objects.equals(search, tag.search) &&
                Objects.equals(counts, tag.counts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagId, type, name, names, place, search, counts);
    }

    @Override
    public String toString() {
        return "Tag{" +
                "tagId='" + tagId + '\'' +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", names=" + names +
                ", place=" + place +
                ", search=" + search +
                ", counts=" + counts +
                ", createdMillis=" + createdMillis +
                ", updatedMillis=" + updatedMillis +
                '}';
    }

    /**
     * Place Linked data
     */
    public static class Place {
        private Integer level;
        private Double order;
        private Set<String> remapping;

        public Integer getLevel() {
            return level;
        }

        public void setLevel(Integer level) {
            this.level = level;
        }

        public Double getOrder() {
            return order;
        }

        public void setOrder(Double order) {
            this.order = order;
        }

        public Set<String> getRemapping() {
            return remapping;
        }

        public void setRemapping(Set<String> remapping) {
            this.remapping = remapping;
        }

        @Override
        public String toString() {
            return "Place{" +
                    "level=" + level +
                    ", order=" + order +
                    ", remapping=" + remapping +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Place place = (Place) o;
            return Objects.equals(level, place.level) &&
                    Objects.equals(order, place.order) &&
                    Objects.equals(remapping, place.remapping);
        }

        @Override
        public int hashCode() {

            return Objects.hash(level, order, remapping);
        }
    }

    public static class Search {
        private boolean enabled;
        private boolean listed;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isListed() {
            return listed;
        }

        public void setListed(boolean listed) {
            this.listed = listed;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Search search = (Search) o;
            return enabled == search.enabled &&
                    listed == search.listed;
        }

        @Override
        public int hashCode() {

            return Objects.hash(enabled, listed);
        }

        @Override
        public String toString() {
            return "Search{" +
                    "enabled=" + enabled +
                    ", listed=" + listed +
                    '}';
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
        @JsonProperty("Food")
        Food,

        @JsonProperty("Cuisine")
        Cuisine,

        @JsonProperty("Establishment")
        Establishment,

        @JsonProperty("Amenities")
        Amenities,

        @JsonProperty("Timing")
        Timing,
    }

}