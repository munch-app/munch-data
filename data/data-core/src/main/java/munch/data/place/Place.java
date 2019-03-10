package munch.data.place;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import munch.data.*;
import munch.data.elastic.*;
import munch.data.location.Area;
import munch.data.location.Country;
import munch.data.location.Location;
import munch.file.Image;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.*;
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
public final class Place implements ElasticObject, VersionedObject, SuggestObject, SuggestObject.Names, TimingObject {
    private String placeId;
    private Status status;

    private String name;
    private Set<String> names;
    private List<Tag> tags;

    private String phone;
    private String website;
    private String description;

    private Location location;

    private Menu menu;
    private Price price;
    private Brand brand;

    private List<Hour> hours;
    private List<Image> images;
    private List<Area> areas; // Area data is Managed by Service

    private Long createdMillis;
    private Long updatedMillis;

    private Taste taste;

    @Nullable
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    @NotNull
    @Valid
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    @NotBlank
    @Size(min = 1, max = 255)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    @NotEmpty
    @Valid
    public Set<@Size(min = 1, max = 255) String> getNames() {
        return names;
    }

    public void setNames(Set<String> names) {
        this.names = names;
    }

    @NotNull
    @Valid
    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    @Nullable
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Nullable
    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NotNull
    @Valid
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Nullable
    @Valid
    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    @Nullable
    @Valid
    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    @Valid
    @Nullable
    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    @NotNull
    @Valid
    public List<Hour> getHours() {
        return hours;
    }

    public void setHours(List<Hour> hours) {
        this.hours = hours;
    }

    @NotNull
    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    @NotNull
    public List<Area> getAreas() {
        return areas;
    }

    public void setAreas(List<Area> areas) {
        this.areas = areas;
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

    @NotNull
    @Valid
    public Taste getTaste() {
        return taste;
    }

    public void setTaste(Taste taste) {
        this.taste = taste;
    }

    @Override
    public String toString() {
        return "Place{" +
                "placeId='" + placeId + '\'' +
                ", status=" + status +
                ", name='" + name + '\'' +
                ", names=" + names +
                ", tags=" + tags +
                ", phone='" + phone + '\'' +
                ", website='" + website + '\'' +
                ", description='" + description + '\'' +
                ", location=" + location +
                ", menu=" + menu +
                ", price=" + price +
                ", brand=" + brand +
                ", hours=" + hours +
                ", images=" + images +
                ", areas=" + areas +
                ", createdMillis=" + createdMillis +
                ", updatedMillis=" + updatedMillis +
                ", taste=" + taste +
                '}';
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Tag {
        private String tagId;
        private String name;
        private munch.data.tag.Tag.Type type;

        @NotBlank
        public String getTagId() {
            return tagId;
        }

        public void setTagId(String tagId) {
            this.tagId = tagId;
        }

        @NotBlank
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @NotNull
        public munch.data.tag.Tag.Type getType() {
            return type;
        }

        public void setType(munch.data.tag.Tag.Type type) {
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Tag tag = (Tag) o;
            return Objects.equals(tagId, tag.tagId) &&
                    Objects.equals(name, tag.name) &&
                    type == tag.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(tagId, name, type);
        }

        @Override
        public String toString() {
            return "Tag{" +
                    "tagId='" + tagId + '\'' +
                    ", name='" + name + '\'' +
                    ", type=" + type +
                    '}';
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Brand {
        private String brandId;
        private String name;

        @NotBlank
        public String getBrandId() {
            return brandId;
        }

        public void setBrandId(String brandId) {
            this.brandId = brandId;
        }

        @NotBlank
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Brand brand = (Brand) o;
            return Objects.equals(brandId, brand.brandId) &&
                    Objects.equals(name, brand.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(brandId, name);
        }

        @Override
        public String toString() {
            return "Brand{" +
                    "brandId='" + brandId + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Menu {
        private String url;
        private List<Image> images;

        @Nullable
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @NotNull
        public List<Image> getImages() {
            return images;
        }

        public void setImages(List<Image> images) {
            this.images = images;
        }

        @Override
        public String toString() {
            return "Menu{" +
                    "url='" + url + '\'' +
                    ", images=" + images +
                    '}';
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Price {
        private double perPax;

        public double getPerPax() {
            return perPax;
        }

        public void setPerPax(double perPax) {
            this.perPax = perPax;
        }

        @Override
        public String toString() {
            return "Price{" +
                    "perPax=" + perPax +
                    '}';
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        private Type type;
        private Moved moved;
        private Renamed renamed;
        private Redirected redirected;

        @NotNull
        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        @Nullable
        @Valid
        public Moved getMoved() {
            return moved;
        }

        public void setMoved(Moved moved) {
            this.moved = moved;
        }

        @Nullable
        @Valid
        public Renamed getRenamed() {
            return renamed;
        }

        public void setRenamed(Renamed renamed) {
            this.renamed = renamed;
        }

        @Nullable
        @Valid
        public Redirected getRedirected() {
            return redirected;
        }

        public void setRedirected(Redirected redirected) {
            this.redirected = redirected;
        }

        public enum Type {
            open,
            renovation,
            closed,

            moved,
            deleted,
            renamed,
            redirected,
        }

        public static class Moved {
            private String placeId;

            @NotBlank
            public String getPlaceId() {
                return placeId;
            }

            public void setPlaceId(String placeId) {
                this.placeId = placeId;
            }

            @Override
            public String toString() {
                return "Moved{" +
                        "placeId='" + placeId + '\'' +
                        '}';
            }
        }

        public static class Renamed {
            private String placeId;
            private String name;

            @NotBlank
            public String getPlaceId() {
                return placeId;
            }

            public void setPlaceId(String placeId) {
                this.placeId = placeId;
            }

            @NotBlank
            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            @Override
            public String toString() {
                return "Renamed{" +
                        "placeId='" + placeId + '\'' +
                        ", name='" + name + '\'' +
                        '}';
            }
        }

        public static class Redirected {
            private String placeId;

            @NotBlank
            public String getPlaceId() {
                return placeId;
            }

            public void setPlaceId(String placeId) {
                this.placeId = placeId;
            }

            @Override
            public String toString() {
                return "Redirected{" +
                        "placeId='" + placeId + '\'' +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "Status{" +
                    "type=" + type +
                    ", moved=" + moved +
                    ", renamed=" + renamed +
                    ", redirected=" + redirected +
                    '}';
        }
    }

    /**
     * Global taste summary from munch-taste service/system
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Taste {
        private int group;
        private double importance;

        /**
         * Group 0 is fallthrough, all place data should avoid getting into group 0
         * Group 1 is high computability data, meaning frequently user might go
         * Group 2 is high discoverability data, meaning occasionally user might go
         *
         * @return from 0 - 2, higher = better
         */
        public int getGroup() {
            return group;
        }

        public void setGroup(int group) {
            this.group = group;
        }

        /**
         * @return from 0.0 - 1.0, higher = better
         */
        public double getImportance() {
            return importance;
        }

        public void setImportance(double importance) {
            this.importance = importance;
        }

        @Override
        public String toString() {
            return "Taste{" +
                    "group=" + group +
                    ", importance=" + importance +
                    '}';
        }
    }

    @Override
    @Nullable
    @JsonIgnore
    public Context getSuggestContext() {
        switch (getStatus().getType()) {
            case renovation:
            case moved:
            case deleted:
            case renamed:
            case redirected:
            default:
                return null;

            case open:
            case closed:
        }

        return new Context() {
            @Override
            public Set<Country> getCountries() {
                return Set.of(getLocation().getCountry());
            }

            @Override
            public String getLatLng() {
                return getLocation().getLatLng();
            }

            @Override
            public int getWeight() {
                // Group starts: 0 - 2, 0 is lowest hence always + 1
                return getTaste().getGroup() + 1;
            }
        };
    }

    @Override
    @JsonIgnore
    public String getDataId() {
        return placeId;
    }

    @Override
    public DataType getDataType() {
        return DataType.Place;
    }


    @Override
    public String getVersion() {
        return "2019-03-10";
    }
}
