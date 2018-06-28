package munch.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import munch.data.location.Landmark;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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

    private String city;
    private String country;
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

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

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
        @NotNull
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
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", postcode='" + postcode + '\'' +
                ", latLng='" + latLng + '\'' +
                ", polygon=" + polygon +
                ", landmarks=" + landmarks +
                '}';
    }


}
