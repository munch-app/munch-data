package munch.data.place.parser.location;

/**
 * Created by: Fuxing
 * Date: 28/7/2017
 * Time: 3:13 PM
 * Project: munch-corpus
 */
public final class TrainStation {
    private String name;
    private double lat;
    private double lng;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    @Override
    public String toString() {
        return "TrainStation{" +
                "name='" + name + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                '}';
    }
}
