package munch.data.location;

import java.util.List;

/**
 * Created by: Fuxing
 * Date: 27/3/2018
 * Time: 1:39 AM
 * Project: munch-data
 */
public final class LocationData {
    private final List<String> tokens;

    private String address;
    private String street;
    private String unitNumber;

    private String neighbourhood;
    private String city;
    private String country;

    private String postal;
    private String latLng;

    public LocationData(List<String> tokens) {
        this.tokens = tokens;
    }

    public List<String> getTokens() {
        return tokens;
    }

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

    public double getAccuracy() {
        double accuracy = 0;
        if (address != null) accuracy++;
        if (street != null) accuracy++;
        if (unitNumber != null) accuracy++;

        if (neighbourhood != null) accuracy++;
        if (city != null) accuracy++;
        if (country != null) accuracy++;

        if (postal != null) accuracy++;
        if (latLng != null) accuracy++;
        return accuracy;
    }

    @Override
    public String toString() {
        return "LocationData{" +
                "address='" + address + '\'' +
                ", street='" + street + '\'' +
                ", unitNumber='" + unitNumber + '\'' +
                ", neighbourhood='" + neighbourhood + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", postal='" + postal + '\'' +
                ", latLng='" + latLng + '\'' +
                '}';
    }
}
