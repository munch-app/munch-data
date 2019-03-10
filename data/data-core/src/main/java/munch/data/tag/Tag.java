package munch.data.tag;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import munch.data.elastic.DataType;
import munch.data.elastic.ElasticObject;
import munch.data.elastic.SuggestObject;
import munch.data.elastic.VersionedObject;
import munch.data.location.Country;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
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
public final class Tag implements ElasticObject, VersionedObject, SuggestObject, SuggestObject.Names {
    private String tagId;

    private Type type;
    private String name;
    private Set<String> names;

    private Place place;

    // To Deprecate
    private Search search;
    private Counts counts;

    private Long createdMillis;
    private Long updatedMillis;

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
    @Pattern(regexp = "[a-zA-Z0-9& -]{1,64}")
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
    @Override
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

    @NotNull
    @Override
    public Long getCreatedMillis() {
        return createdMillis;
    }

    public void setCreatedMillis(Long createdMillis) {
        this.createdMillis = createdMillis;
    }

    @NotNull
    @Override
    public Long getUpdatedMillis() {
        return updatedMillis;
    }

    public void setUpdatedMillis(Long updatedMillis) {
        this.updatedMillis = updatedMillis;
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
    @Deprecated
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
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

    @Deprecated
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
        Food,

        Cuisine,

        Establishment,

        Amenities,

        Timing,

        Requirement,
    }


    @Override
    public DataType getDataType() {
        return DataType.Tag;
    }

    @Override
    @JsonIgnore
    public String getDataId() {
        return tagId;
    }

    @Override
    public String getVersion() {
        return "2019-03-10";
    }

    @Override
    @JsonIgnore
    public Context getSuggestContext() {
        return new Context() {
            // TODO in the future: Extract from new Localization Object
            @Override
            public Set<Country> getCountries() {
                return Set.of(Country.SGP);
            }
        };
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Localization {
        private Country country;
        // Display: Search, List
    }
}
