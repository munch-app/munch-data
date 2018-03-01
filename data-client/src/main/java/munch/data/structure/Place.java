package munch.data.structure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.math.DoubleMath;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 7/3/2017
 * Time: 5:54 PM
 * Project: munch-core
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Place implements SearchResult {
    private String id;
    private String version;

    // Basic
    private String name;
    private Set<String> allNames;
    private String phone;
    private String website;
    private String description;

    // One
    private Price price;
    private Location location;
    private Review review;
    private Tag tag;

    // Many
    private List<Hour> hours;
    private List<SourcedImage> images;
    private List<Container> containers;

    // Others
    private Date createdDate;
    private Date updatedDate; // Must in hashcode or equals
    private double ranking;

    /**
     * @return place id, provided by catalyst groupId
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return version of this data structure
     */
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return name of the place, trim if over
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return all possible name of place
     */
    public Set<String> getAllNames() {
        return allNames;
    }

    public void setAllNames(Set<String> allNames) {
        this.allNames = allNames;
    }

    /**
     * Preferably with country code intact
     *
     * @return phone number of the place
     */
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Website url must not exceed 2000 character.
     * Trim if necessary
     * http://stackoverflow.com/questions/417142/what-is-the-maximum-length-of-a-url-in-different-browsers
     *
     * @return website url of place
     */
    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    /**
     * Description of place must not exceed 1000 character.
     * Trim if over 1000 characters
     *
     * @return description of place
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * @return nullable review
     */
    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public List<Hour> getHours() {
        return hours;
    }

    public void setHours(List<Hour> hours) {
        this.hours = hours;
    }

    /**
     * @return some images of place from other source
     */
    public List<SourcedImage> getImages() {
        return images;
    }

    public void setImages(List<SourcedImage> images) {
        this.images = images;
    }

    public List<Container> getContainers() {
        return containers;
    }

    public void setContainers(List<Container> containers) {
        this.containers = containers;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public double getRanking() {
        return ranking;
    }

    public void setRanking(double ranking) {
        this.ranking = ranking;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Place place = (Place) o;
        return Double.compare(place.ranking, ranking) == 0 &&
                Objects.equals(id, place.id) &&
                Objects.equals(version, place.version) &&
                Objects.equals(name, place.name) &&
                Objects.equals(phone, place.phone) &&
                Objects.equals(website, place.website) &&
                Objects.equals(description, place.description) &&
                Objects.equals(price, place.price) &&
                Objects.equals(location, place.location) &&
                Objects.equals(review, place.review) &&
                Objects.equals(tag, place.tag) &&
                Objects.equals(hours, place.hours) &&
                Objects.equals(images, place.images) &&
                Objects.equals(containers, place.containers) &&
                Objects.equals(createdDate, place.createdDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, name, phone, website, description, price, location, review, tag, hours, images, containers, createdDate, ranking);
    }

    @Override
    public String toString() {
        return "Place{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", website='" + website + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", location=" + location +
                ", review=" + review +
                ", tag=" + tag +
                ", hours=" + hours +
                ", images=" + images +
                ", containers=" + containers +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                ", ranking=" + ranking +
                '}';
    }

    @Override
    public String getDataType() {
        return "Place";
    }

    /**
     * Container list generated must be sorted properly or else .equals will fail
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Container {
        private String id;
        private String type;
        private String name;

        private List<SourcedImage> images;
        private double ranking;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<SourcedImage> getImages() {
            return images;
        }

        public void setImages(List<SourcedImage> images) {
            this.images = images;
        }

        public double getRanking() {
            return ranking;
        }

        public void setRanking(double ranking) {
            this.ranking = ranking;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Container container = (Container) o;
            return Double.compare(container.ranking, ranking) == 0 &&
                    Objects.equals(id, container.id) &&
                    Objects.equals(type, container.type) &&
                    Objects.equals(name, container.name) &&
                    Objects.equals(images, container.images);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, type, name, images, ranking);
        }

        @Override
        public String toString() {
            return "Container{" +
                    "id='" + id + '\'' +
                    ", type='" + type + '\'' +
                    ", name='" + name + '\'' +
                    ", images=" + images +
                    ", ranking=" + ranking +
                    '}';
        }
    }

    /**
     * Location data of the place
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Location {
        private String street;
        private String address;
        private String unitNumber;

        // Landmarks nearby
        private List<Landmark> landmarks;

        private String neighbourhood;
        private String city;
        private String country;

        private String postal;
        private String latLng;

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getUnitNumber() {
            return unitNumber;
        }

        public void setUnitNumber(String unitNumber) {
            this.unitNumber = unitNumber;
        }

        /**
         * @return landmarks nearby
         */
        public List<Landmark> getLandmarks() {
            return landmarks;
        }

        public void setLandmarks(List<Landmark> landmarks) {
            this.landmarks = landmarks;
        }

        public String getNeighbourhood() {
            return neighbourhood;
        }

        public void setNeighbourhood(String neighbourhood) {
            this.neighbourhood = neighbourhood;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getPostal() {
            return postal;
        }

        public void setPostal(String postal) {
            this.postal = postal;
        }

        public String getLatLng() {
            return latLng;
        }

        public void setLatLng(String latLng) {
            this.latLng = latLng;
        }

        public void setLatLng(double lat, double lng) {
            setLatLng(lat + "," + lng);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Location location = (Location) o;
            return Objects.equals(street, location.street) &&
                    Objects.equals(address, location.address) &&
                    Objects.equals(unitNumber, location.unitNumber) &&
                    Objects.equals(landmarks, location.landmarks) &&
                    Objects.equals(neighbourhood, location.neighbourhood) &&
                    Objects.equals(city, location.city) &&
                    Objects.equals(country, location.country) &&
                    Objects.equals(postal, location.postal) &&
                    Objects.equals(latLng, location.latLng);
        }

        @Override
        public int hashCode() {
            return Objects.hash(street, address, unitNumber, landmarks, neighbourhood, city, country, postal, latLng);
        }

        @Override
        public String toString() {
            return "Location{" +
                    "street='" + street + '\'' +
                    ", address='" + address + '\'' +
                    ", unitNumber='" + unitNumber + '\'' +
                    ", landmarks=" + landmarks +
                    ", neighbourhood='" + neighbourhood + '\'' +
                    ", city='" + city + '\'' +
                    ", country='" + country + '\'' +
                    ", postal='" + postal + '\'' +
                    ", latLng='" + latLng + '\'' +
                    '}';
        }

        public static final class Landmark {
            private String name;
            private String type; // types: ["train"]
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
        }
    }

    /**
     * Tag of place
     * Both tags are applied when search with different weight
     * <p>
     * Explicits are visible tags
     * Implicits are invisible tags (plus all visible tags), search on invisible to find everything
     * <p>
     * List is used instead to maintain order
     */
    public static final class Tag {
        private List<String> explicits;
        private List<String> implicits;

        public List<String> getExplicits() {
            return explicits;
        }

        public void setExplicits(List<String> explicits) {
            this.explicits = explicits;
        }

        public List<String> getImplicits() {
            return implicits;
        }

        public void setImplicits(List<String> implicits) {
            this.implicits = implicits;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Tag tag = (Tag) o;
            return Objects.equals(explicits, tag.explicits) &&
                    Objects.equals(implicits, tag.implicits);
        }

        @Override
        public int hashCode() {
            return Objects.hash(explicits, implicits);
        }

        @Override
        public String toString() {
            return "Tag{" +
                    "explicits=" + explicits +
                    ", implicits=" + implicits +
                    '}';
        }
    }

    /**
     * Review of a place
     */
    public static final class Review {
        private long total;
        private double average;

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public double getAverage() {
            return average;
        }

        public void setAverage(double average) {
            this.average = average;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Review review = (Review) o;

            if (total != review.total) return false;
            return DoubleMath.fuzzyEquals(review.average, average, 0.05);
        }

        @Override
        public int hashCode() {
            return Objects.hash(total, average);
        }

        @Override
        public String toString() {
            return "Review{" +
                    "total=" + total +
                    ", average=" + average +
                    '}';
        }
    }

    /**
     * Price data of the place
     */
    public static final class Price {
        private Double lowest;
        private Double middle; // Average per Pax
        private Double highest;

        /**
         * Part of price range
         *
         * @return lowest in price range
         */
        @Deprecated
        public Double getLowest() {
            return lowest;
        }

        public void setLowest(Double lowest) {
            this.lowest = lowest;
        }

        /**
         * @return highest - lowest/2 + lowest
         */
        public Double getMiddle() {
            return middle;
        }

        public void setMiddle(Double middle) {
            this.middle = middle;
        }

        /**
         * Part of price range
         *
         * @return highest in price range
         */
        @Deprecated
        public Double getHighest() {
            return highest;
        }

        public void setHighest(Double highest) {
            this.highest = highest;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Price price = (Price) o;
            if (!comparePrice(lowest, price.lowest)) return false;
            if (!comparePrice(middle, price.middle)) return false;
            return comparePrice(highest, price.highest);
        }

        public boolean comparePrice(Double left, Double right) {
            if (left != null && right != null) {
                return DoubleMath.fuzzyEquals(left, right, 0.05);
            }
            // One of them is null, if both null means same
            return left == null && right == null;
        }

        @Override
        public int hashCode() {
            return Objects.hash(lowest, middle, highest);
        }

        @Override
        public String toString() {
            return "Price{" +
                    "lowest=" + lowest +
                    ", middle=" + middle +
                    ", highest=" + highest +
                    '}';
        }
    }

    /**
     * Opening hour of the place
     */
    public static final class Hour {
        private String day;

        /**
         * HH:mm
         * 00:00 - 23:59
         * 00:00 - 24:00 (Allowed)
         * Midnight - 1 Min before midnight Max
         * <p>
         * 12:00 - 22:00
         * Noon - 10pm
         * <p>
         * 08:00 - 19:00
         * 8am - 7pm
         * <p>
         * Not allowed to put 24:00 Highest is 23:59
         * Not allowed to cross to another day
         */
        private String open;
        private String close;

        /**
         * mon
         * tue
         * wed
         * thu
         * fri
         * sat
         * sun
         * ph
         * evePh
         *
         * @return day that will be in string
         */
        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        /**
         * @return opening hours
         */
        public String getOpen() {
            return open;
        }

        public void setOpen(String open) {
            this.open = open;
        }

        /**
         * @return closing hours
         */
        public String getClose() {
            return close;
        }

        public void setClose(String close) {
            this.close = close;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Hour hour = (Hour) o;
            return Objects.equals(day, hour.day) &&
                    Objects.equals(open, hour.open) &&
                    Objects.equals(close, hour.close);
        }

        @Override
        public int hashCode() {
            return Objects.hash(day, open, close);
        }

        @Override
        public String toString() {
            return "Hour{" +
                    "day='" + day + '\'' +
                    ", open='" + open + '\'' +
                    ", close='" + close + '\'' +
                    '}';
        }
    }

}
