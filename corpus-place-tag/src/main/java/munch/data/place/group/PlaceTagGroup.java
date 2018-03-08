package munch.data.place.group;

import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 15/10/2017
 * Time: 2:13 AM
 * Project: munch-data
 */
public final class PlaceTagGroup {
    private final String recordId;

    private String name;
    private String type;
    private double order;

    private Set<String> converts;
    private Set<String> synonyms;

    public PlaceTagGroup(String recordId, String name) {
        this.recordId = recordId;
        this.name = name;
    }

    public String getRecordId() {
        return recordId;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getOrder() {
        return order;
    }

    public void setOrder(double order) {
        this.order = order;
    }

    public Set<String> getConverts() {
        return converts;
    }

    public void setConverts(Set<String> converts) {
        this.converts = converts;
    }

    public Set<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<String> synonyms) {
        this.synonyms = synonyms;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlaceTagGroup tag = (PlaceTagGroup) o;

        return name.equals(tag.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}