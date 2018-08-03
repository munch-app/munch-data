package munch.data.place;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by: Fuxing
 * Date: 4/8/18
 * Time: 12:51 AM
 * Project: munch-data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PlaceAward {
    private String placeId;
    private String awardId;

    private String name;
    private String description;

    private Long sort;

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getAwardId() {
        return awardId;
    }

    public void setAwardId(String awardId) {
        this.awardId = awardId;
    }

    @NotNull
    @Size(min = 3, max = 100)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    @Size(min = 3, max = 500)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NotNull
    public Long getSort() {
        return sort;
    }

    public void setSort(Long sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        return "PlaceAward{" +
                "placeId='" + placeId + '\'' +
                ", awardId='" + awardId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", sort=" + sort +
                '}';
    }
}
