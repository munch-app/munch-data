package munch.data.place;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import munch.data.*;
import munch.data.location.Cluster;
import munch.file.Image;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
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
public final class Place implements ElasticObject, VersionedObject, SuggestObject, CorrectableObject {
    private String placeId;
    private Status status;

    private String name;
    private Set<String> names;
    private List<String> tags;

    private String website;
    private String description;

    private Location location;

    private Menu menu;
    private Price price;
    private Counts counts;

    private List<Hour> hours;
    private List<Image> images;
    private List<Cluster> clusters;

    private long createdMillis;
    private long updatedMillis;

    // Deprecate this once TasteBud is ready
    private Double ranking;

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    @NotNull
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
    @NotEmpty
    public Set<String> getNames() {
        return names;
    }

    public void setNames(Set<String> names) {
        this.names = names;
    }

    @NotNull
    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Nullable
    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    @Nullable
    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    @Nullable
    public Counts getCounts() {
        return counts;
    }

    public void setCounts(Counts counts) {
        this.counts = counts;
    }

    @NotNull
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
    public List<Cluster> getClusters() {
        return clusters;
    }

    public void setClusters(List<Cluster> clusters) {
        this.clusters = clusters;
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

    @NotNull
    public Double getRanking() {
        return ranking;
    }

    public void setRanking(Double ranking) {
        this.ranking = ranking;
    }

    @Override
    public String getDataType() {
        return "Place";
    }

    @Override
    public String getVersion() {
        return "2018-05-30";
    }

    @Override
    public String getDataId() {
        return placeId;
    }

    @Override
    public String toString() {
        return "Place{" +
                "placeId='" + placeId + '\'' +
                ", status=" + status +
                ", name='" + name + '\'' +
                ", names=" + names +
                ", tags=" + tags +
                ", website='" + website + '\'' +
                ", description='" + description + '\'' +
                ", location=" + location +
                ", menu=" + menu +
                ", price=" + price +
                ", counts=" + counts +
                ", hours=" + hours +
                ", images=" + images +
                ", clusters=" + clusters +
                ", createdMillis=" + createdMillis +
                ", updatedMillis=" + updatedMillis +
                ", ranking=" + ranking +
                '}';
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Menu {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
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
        private Long updatedMillis;

        @NotNull
        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        @Nullable
        public Moved getMoved() {
            return moved;
        }

        public void setMoved(Moved moved) {
            this.moved = moved;
        }

        @NotNull
        public Long getUpdatedMillis() {
            return updatedMillis;
        }

        public void setUpdatedMillis(Long updatedMillis) {
            this.updatedMillis = updatedMillis;
        }

        public enum Type {
            @JsonProperty("open") open,
            @JsonProperty("renovation") renovation,
            @JsonProperty("closed") closed,
            @JsonProperty("moved") moved
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

        @Override
        public String toString() {
            return "Status{" +
                    "type=" + type +
                    ", moved=" + moved +
                    ", updatedMillis=" + updatedMillis +
                    '}';
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Counts {
        private Article article;
        private Instagram instagram;

        @Nullable
        public Article getArticle() {
            return article;
        }

        public void setArticle(Article article) {
            this.article = article;
        }

        @Nullable
        public Instagram getInstagram() {
            return instagram;
        }

        public void setInstagram(Instagram instagram) {
            this.instagram = instagram;
        }

        @Override
        public String toString() {
            return "Counts{" +
                    "article=" + article +
                    ", instagram=" + instagram +
                    '}';
        }

        public static class Article {
            private long profile;
            private long single;
            private long list;
            private long total;

            public long getProfile() {
                return profile;
            }

            public void setProfile(long profile) {
                this.profile = profile;
            }

            public long getSingle() {
                return single;
            }

            public void setSingle(long single) {
                this.single = single;
            }

            public long getList() {
                return list;
            }

            public void setList(long list) {
                this.list = list;
            }

            public long getTotal() {
                return total;
            }

            public void setTotal(long total) {
                this.total = total;
            }

            @Override
            public String toString() {
                return "Article{" +
                        "profile=" + profile +
                        ", single=" + single +
                        ", list=" + list +
                        ", total=" + total +
                        '}';
            }
        }

        public static class Instagram {
            private long profile;
            private long total;

            public long getProfile() {
                return profile;
            }

            public void setProfile(long profile) {
                this.profile = profile;
            }

            public long getTotal() {
                return total;
            }

            public void setTotal(long total) {
                this.total = total;
            }

            @Override
            public String toString() {
                return "Instagram{" +
                        "profile=" + profile +
                        ", total=" + total +
                        '}';
            }
        }
    }
}
