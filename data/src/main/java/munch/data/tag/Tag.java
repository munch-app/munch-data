package munch.data.tag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import munch.data.CorrectableObject;
import munch.data.ElasticObject;
import munch.data.SuggestObject;
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
public final class Tag implements ElasticObject, VersionedObject, SuggestObject, CorrectableObject {
    private String tagId;

    private String type;
    private String name;
    private Set<String> names;

    private Place place;
    private Count count;

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

    public Count getCount() {
        return count;
    }

    public void setCount(Count count) {
        this.count = count;
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
    }

    public static class Count {
        private Long total;

        public Long getTotal() {
            return total;
        }

        public void setTotal(Long total) {
            this.total = total;
        }

        @Override
        public String toString() {
            return "Count{" +
                    "total=" + total +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Tag{" +
                "tagId='" + tagId + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", names=" + names +
                ", place=" + place +
                ", count=" + count +
                ", createdMillis=" + createdMillis +
                ", updatedMillis=" + updatedMillis +
                '}';
    }
}
