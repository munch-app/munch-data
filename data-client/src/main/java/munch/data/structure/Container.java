package munch.data.structure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 10/12/2017
 * Time: 10:22 AM
 * Project: munch-data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Container implements SearchResult {
    private String id;

    private String name;
    private String type;

    private String phone;
    private String website;
    private String description;

    private Location location;
    private double ranking;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
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
                Objects.equals(name, container.name) &&
                Objects.equals(type, container.type) &&
                Objects.equals(phone, container.phone) &&
                Objects.equals(website, container.website) &&
                Objects.equals(description, container.description) &&
                Objects.equals(location, container.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, phone, website, description, location, ranking);
    }

    @Override
    public String toString() {
        return "Container{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", phone='" + phone + '\'' +
                ", website='" + website + '\'' +
                ", description='" + description + '\'' +
                ", location=" + location +
                ", ranking=" + ranking +
                '}';
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Location {

        private String address;
        private String street;
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
            return Objects.equals(address, location.address) &&
                    Objects.equals(street, location.street) &&
                    Objects.equals(city, location.city) &&
                    Objects.equals(country, location.country) &&
                    Objects.equals(postal, location.postal) &&
                    Objects.equals(latLng, location.latLng);
        }

        @Override
        public int hashCode() {
            return Objects.hash(address, street, city, country, postal, latLng);
        }

        @Override
        public String toString() {
            return "Location{" +
                    "address='" + address + '\'' +
                    ", street='" + street + '\'' +
                    ", city='" + city + '\'' +
                    ", country='" + country + '\'' +
                    ", postal='" + postal + '\'' +
                    ", latLng='" + latLng + '\'' +
                    '}';
        }

    }

    @Override
    public String getDataType() {
        return "Container";
    }
}
