package munch.data.catalyst;

import catalyst.airtable.AirtableRecord;
import com.google.common.collect.ImmutableSet;
import edit.utils.location.SpatialUtils;
import munch.data.location.Location;
import munch.data.location.Area;

import java.util.List;

/**
 * Created by: Fuxing
 * Date: 22/8/18
 * Time: 9:50 PM
 * Project: munch-data
 */
public final class RestrictedArea {
    private String id;
    private String name;
    private Location location;
    private Area.LocationCondition locationCondition;

    public RestrictedArea() {
    }

    public RestrictedArea(AirtableRecord record) {
        this.id = record.getId();
        this.name = record.getFieldString("name");

        this.location = new Location();
        this.location.setCity(record.getFieldString("location.city"));

        List<String> points = SpatialUtils.wktToPoints(record.getFieldString("location.polygon"));
        if (points != null) {
            Location.Polygon polygon = new Location.Polygon();
            polygon.setPoints(points);
            this.location.setPolygon(polygon);
        }

        this.locationCondition = new Area.LocationCondition();
        List<String> unitNumbers = record.getFieldList("locationCondition.unitNumbers", String.class);
        this.locationCondition.setUnitNumbers(ImmutableSet.copyOf(unitNumbers));

        List<String> postcodes = record.getFieldList("locationCondition.postcodes", String.class);
        this.locationCondition.setPostcodes(ImmutableSet.copyOf(postcodes));
    }

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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Area.LocationCondition getLocationCondition() {
        return locationCondition;
    }

    public void setLocationCondition(Area.LocationCondition locationCondition) {
        this.locationCondition = locationCondition;
    }
}
