package munch.data.brand;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import munch.data.*;
import munch.file.Image;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created by: Bing Hwang
 * Date: 10/7/18
 * Time: 12:09 PM
 * Project: munch-data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Brand implements ElasticObject, VersionedObject, SuggestObject, MultipleNameObject {
    private String brandId;
    private Status status;
    private Place place;

    private String name;
    private Set<String> names;
    private List<Tag> tags;

    private String phone;
    private String website;
    private String description;

    private Location location;

    private Menu menu;
    private Price price;
    private Company company;

    private List<Image> images;

    private long createdMillis;
    private long updatedMillis;

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    @Nullable
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    @NotBlank
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    @NotNull
    public Set<String> getNames() {
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

    @Nullable
    @Valid
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
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
    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
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
        return "Brand";
    }

    @Override
    public String getVersion() {
        return "2018-05-30";
    }

    @Override
    public String getDataId() {
        return brandId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Brand brand = (Brand) o;
        return Objects.equals(brandId, brand.brandId) &&
                Objects.equals(name, brand.name) &&
                Objects.equals(names, brand.names) &&
                Objects.equals(tags, brand.tags) &&
                Objects.equals(price, brand.price) &&
                Objects.equals(menu, brand.menu) &&
                Objects.equals(company, brand.company) &&
                Objects.equals(phone, brand.phone) &&
                Objects.equals(website, brand.website) &&
                Objects.equals(description, brand.description) &&
                Objects.equals(place, brand.place) &&
                Objects.equals(images, brand.images);
    }

    @Override
    public int hashCode() {
        return Objects.hash(brandId, name, names, tags, price, menu, company, phone, website, description, place, images);
    }

    @Override
    public String toString() {
        return "Brand{" +
                "brandId='" + brandId + '\'' +
                ", status=" + status +
                ", place=" + place +
                ", name='" + name + '\'' +
                ", names=" + names +
                ", tags=" + tags +
                ", phone='" + phone + '\'' +
                ", website='" + website + '\'' +
                ", description='" + description + '\'' +
                ", location=" + location +
                ", menu=" + menu +
                ", price=" + price +
                ", company=" + company +
                ", images=" + images +
                ", createdMillis=" + createdMillis +
                ", updatedMillis=" + updatedMillis +
                '}';
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        private Type type;

        @NotNull
        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public enum Type {
            @JsonProperty("open") open,
            @JsonProperty("closed") closed,
        }

        @Override
        public String toString() {
            return "Status{" +
                    "type=" + type +
                    '}';
        }
    }

    /**
     * Place Linked data
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Place {
        private boolean autoLink;

        public boolean isAutoLink() {
            return autoLink;
        }

        public void setAutoLink(boolean autoLink) {
            this.autoLink = autoLink;
        }

        @Override
        public String toString() {
            return "Place{" +
                    "autoLink=" + autoLink +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Place place = (Place) o;
            return autoLink == place.autoLink;
        }

        @Override
        public int hashCode() {
            return Objects.hash(autoLink);
        }
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
    public static class Menu {
        private String url;

        @Nullable
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Menu menu = (Menu) o;
            return Objects.equals(url, menu.url);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url);
        }

        @Override
        public String toString() {
            return "Menu{" +
                    "url='" + url + '\'' +
                    '}';
        }
    }

    public static class Price {
        private double perPax;

        public double getPerPax() {
            return perPax;
        }

        public void setPerPax(double perPax) {
            this.perPax = perPax;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Price price = (Price) o;
            return Double.compare(price.perPax, perPax) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(perPax);
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
    public static class Company {
        private String name;

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
            Company company = (Company) o;
            return Objects.equals(name, company.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return "Company{" +
                    "name=" + name +
                    '}';
        }
    }

}
