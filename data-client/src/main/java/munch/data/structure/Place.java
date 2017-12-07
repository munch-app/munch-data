package munch.data.structure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.math.DoubleMath;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    // Basic
    private String name;
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
    private List<Image> images;

    // Others
    private Date createdDate;
    private Date updatedDate;
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
     * @return name of the place, trim if over
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
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

        if (Double.compare(place.ranking, ranking) != 0) return false;
        if (!id.equals(place.id)) return false;
        if (!name.equals(place.name)) return false;
        if (phone != null ? !phone.equals(place.phone) : place.phone != null) return false;
        if (website != null ? !website.equals(place.website) : place.website != null) return false;
        if (description != null ? !description.equals(place.description) : place.description != null) return false;
        if (price != null ? !price.equals(place.price) : place.price != null) return false;
        if (!location.equals(place.location)) return false;
        if (review != null ? !review.equals(place.review) : place.review != null) return false;
        if (tag != null ? !tag.equals(place.tag) : place.tag != null) return false;
        if (hours != null ? !hours.equals(place.hours) : place.hours != null) return false;
        if (images != null ? !images.equals(place.images) : place.images != null) return false;
        return createdDate.equals(place.createdDate);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (website != null ? website.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (price != null ? price.hashCode() : 0);
        result = 31 * result + location.hashCode();
        result = 31 * result + (review != null ? review.hashCode() : 0);
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        result = 31 * result + (hours != null ? hours.hashCode() : 0);
        result = 31 * result + (images != null ? images.hashCode() : 0);
        result = 31 * result + createdDate.hashCode();
        temp = Double.doubleToLongBits(ranking);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
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
     * Location data of the place
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Location {
        private String street;
        private String address;
        private String unitNumber;
        private String building;
        private String nearestTrain;

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

        public String getBuilding() {
            return building;
        }

        public void setBuilding(String building) {
            this.building = building;
        }

        public String getNearestTrain() {
            return nearestTrain;
        }

        public void setNearestTrain(String nearestTrain) {
            this.nearestTrain = nearestTrain;
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

            if (street != null ? !street.equals(location.street) : location.street != null) return false;
            if (address != null ? !address.equals(location.address) : location.address != null) return false;
            if (unitNumber != null ? !unitNumber.equals(location.unitNumber) : location.unitNumber != null)
                return false;
            if (building != null ? !building.equals(location.building) : location.building != null) return false;
            if (nearestTrain != null ? !nearestTrain.equals(location.nearestTrain) : location.nearestTrain != null)
                return false;
            if (city != null ? !city.equals(location.city) : location.city != null) return false;
            if (country != null ? !country.equals(location.country) : location.country != null) return false;
            if (postal != null ? !postal.equals(location.postal) : location.postal != null) return false;
            return latLng != null ? latLng.equals(location.latLng) : location.latLng == null;
        }

        @Override
        public int hashCode() {
            int result = street != null ? street.hashCode() : 0;
            result = 31 * result + (address != null ? address.hashCode() : 0);
            result = 31 * result + (unitNumber != null ? unitNumber.hashCode() : 0);
            result = 31 * result + (building != null ? building.hashCode() : 0);
            result = 31 * result + (nearestTrain != null ? nearestTrain.hashCode() : 0);
            result = 31 * result + (city != null ? city.hashCode() : 0);
            result = 31 * result + (country != null ? country.hashCode() : 0);
            result = 31 * result + (postal != null ? postal.hashCode() : 0);
            result = 31 * result + (latLng != null ? latLng.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Location{" +
                    "street='" + street + '\'' +
                    ", address='" + address + '\'' +
                    ", unitNumber='" + unitNumber + '\'' +
                    ", building='" + building + '\'' +
                    ", nearestTrain='" + nearestTrain + '\'' +
                    ", city='" + city + '\'' +
                    ", country='" + country + '\'' +
                    ", postal='" + postal + '\'' +
                    ", latLng='" + latLng + '\'' +
                    '}';
        }
    }

    /**
     * Tag of place
     * Both tags are applied when search with different weight
     * <p>
     * Explicits are visible tags
     * Implicits are invisible tags
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

            if (!explicits.equals(tag.explicits)) return false;
            return implicits.equals(tag.implicits);
        }

        @Override
        public int hashCode() {
            int result = explicits.hashCode();
            result = 31 * result + implicits.hashCode();
            return result;
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
            int result;
            long temp;
            result = (int) (total ^ (total >>> 32));
            temp = Double.doubleToLongBits(average);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
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
        private Double middle; // AKA per pax
        private Double highest;

        /**
         * Part of price range
         *
         * @return lowest in price range
         */
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
            int result = lowest != null ? lowest.hashCode() : 0;
            result = 31 * result + (middle != null ? middle.hashCode() : 0);
            result = 31 * result + (highest != null ? highest.hashCode() : 0);
            return result;
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
    }

    /**
     * Technically this is a smaller subclass of ImageMeta in munch-images
     * with lesser fields
     */
    public static final class Image {
        private double weight;
        private String source;
        private Map<String, String> images;

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        /**
         * different types of images
         *
         * @return type->url
         */
        public Map<String, String> getImages() {
            return images;
        }

        public void setImages(Map<String, String> images) {
            this.images = images;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Image image = (Image) o;

            if (!DoubleMath.fuzzyEquals(image.weight, weight, 0.05)) return false;
            if (!source.equals(image.source)) return false;
            return images.equals(image.images);
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            temp = Double.doubleToLongBits(weight);
            result = (int) (temp ^ (temp >>> 32));
            result = 31 * result + source.hashCode();
            result = 31 * result + images.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Image{" +
                    "weight=" + weight +
                    ", source='" + source + '\'' +
                    ", images=" + images +
                    '}';
        }
    }
}
