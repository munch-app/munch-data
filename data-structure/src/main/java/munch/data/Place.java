package munch.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;
import java.util.List;

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

    // Many
    private List<String> tags;
    private List<Hour> hours;
    private List<Image> images;

    // Others
    private Date createdDate;
    private Date updatedDate;
    private double munchRank;

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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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

    public double getMunchRank() {
        return munchRank;
    }

    public void setMunchRank(double munchRank) {
        this.munchRank = munchRank;
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
                ", tags=" + tags +
                ", hours=" + hours +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
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

        @Override
        public String toString() {
            return "Location{" +
                    "street='" + street + '\'' +
                    ", address='" + address + '\'' +
                    ", unitNumber='" + unitNumber + '\'' +
                    ", building='" + building + '\'' +
                    ", city='" + city + '\'' +
                    ", country='" + country + '\'' +
                    ", postal='" + postal + '\'' +
                    ", latLng='" + latLng + '\'' +
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
        public String toString() {
            return "Price{" +
                    "lowest=" + lowest +
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
         * Midnight - 1 Min before midnight Max
         * <p>
         * 12:00 - 22:00
         * Noon - 10pm
         * <p>
         * 08:00 - 19:00
         * 8am - 7pm
         * <p>
         * Now Allowed:
         * 20:00 - 04:00
         * 20:00 - 24:00
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

            if (day != hour.day) return false;
            if (!open.equals(hour.open)) return false;
            return close.equals(hour.close);
        }

        @Override
        public int hashCode() {
            int result = day.hashCode();
            result = 31 * result + open.hashCode();
            result = 31 * result + close.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Hour{" +
                    "day=" + day +
                    ", open='" + open + '\'' +
                    ", close='" + close + '\'' +
                    '}';
        }
    }

    /**
     * Technically this is a smaller subclass of ImageMeta in munch-images
     * with lesser fields
     */
    public static final class Image {
        private String source;
        private ImageMeta imageMeta;

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public ImageMeta getImageMeta() {
            return imageMeta;
        }

        public void setImageMeta(ImageMeta imageMeta) {
            this.imageMeta = imageMeta;
        }
    }
}
