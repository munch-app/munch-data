package munch.data.catalyst;

import catalyst.airtable.AirtableRecord;
import com.google.common.collect.ImmutableSet;
import munch.data.location.Location;

import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 27/8/2018
 * Time: 8:11 PM
 * Project: munch-data
 */
public final class Concept {
    private String id;
    private String name;
    private Set<String> equals;
    private Set<String> contains;
    private Set<String> tags;
    private Location location;

    public Concept() {
    }

    public Concept(AirtableRecord record) {
        this.id = record.getId();
        this.name = record.getFieldString("name");
        this.equals = ImmutableSet.copyOf(record.getFieldList("equals", String.class));
        this.contains = ImmutableSet.copyOf(record.getFieldList("contains", String.class));
        this.tags = ImmutableSet.copyOf(record.getFieldList("tags", String.class));

        this.location = new Location();
        this.location.setCountry(record.getFieldString("location.country"));
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

    public Set<String> getEquals() {
        return equals;
    }

    public void setEquals(Set<String> equals) {
        this.equals = equals;
    }

    public Set<String> getContains() {
        return contains;
    }

    public void setContains(Set<String> contains) {
        this.contains = contains;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
