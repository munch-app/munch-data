package munch.data.location;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 31/5/18
 * Time: 9:49 PM
 * Project: munch-data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Location {
    private String address;
    private String street;
    private String unitNumber;
    private String neighbourhood;

    private City city;
    private Country country;
    private String postcode;

    private String latLng;
    private Polygon polygon;

    private List<Landmark> landmarks;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getUnitNumber() {
        return unitNumber;
    }

    public void setUnitNumber(String unitNumber) {
        this.unitNumber = unitNumber;
    }

    public String getNeighbourhood() {
        return neighbourhood;
    }

    public void setNeighbourhood(String neighbourhood) {
        this.neighbourhood = neighbourhood;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public void setCity(String city) {
        try {
            this.city = City.valueOf(city);
        } catch (IllegalArgumentException e) {
            this.city = null;
        }
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public void setCountry(String country) {
        try {
            this.country = Country.valueOf(country);
        } catch (IllegalArgumentException e) {
            this.country = null;
        }
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    @Nullable
    @Pattern(regexp = "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)$")
    public String getLatLng() {
        return latLng;
    }

    public void setLatLng(String latLng) {
        this.latLng = latLng;
    }

    @Valid
    @Nullable
    public Polygon getPolygon() {
        return polygon;
    }

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }

    public List<Landmark> getLandmarks() {
        return landmarks;
    }

    public void setLandmarks(List<Landmark> landmarks) {
        this.landmarks = landmarks;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Polygon {
        private List<String> points;

        @NotEmpty
        public List<String> getPoints() {
            return points;
        }

        public void setPoints(List<String> points) {
            this.points = points;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Polygon polygon = (Polygon) o;
            return Objects.equals(points, polygon.points);
        }

        @Override
        public int hashCode() {
            return Objects.hash(points);
        }

        @Override
        public String toString() {
            return "Polygon{" +
                    "points=" + points +
                    '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(address, location.address) &&
                Objects.equals(street, location.street) &&
                Objects.equals(unitNumber, location.unitNumber) &&
                Objects.equals(neighbourhood, location.neighbourhood) &&
                Objects.equals(city, location.city) &&
                Objects.equals(country, location.country) &&
                Objects.equals(postcode, location.postcode) &&
                Objects.equals(latLng, location.latLng) &&
                Objects.equals(polygon, location.polygon) &&
                Objects.equals(landmarks, location.landmarks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, street, unitNumber, neighbourhood, city, country, postcode, latLng, polygon, landmarks);
    }

    @Override
    public String toString() {
        return "Location{" +
                "address='" + address + '\'' +
                ", street='" + street + '\'' +
                ", unitNumber='" + unitNumber + '\'' +
                ", neighbourhood='" + neighbourhood + '\'' +
                ", city=" + city +
                ", country=" + country +
                ", postcode='" + postcode + '\'' +
                ", latLng='" + latLng + '\'' +
                ", polygon=" + polygon +
                ", landmarks=" + landmarks +
                '}';
    }
}
