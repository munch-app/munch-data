package munch.data.place.group;

import java.util.Objects;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 15/10/2017
 * Time: 2:13 AM
 * Project: munch-data
 */
public final class PlaceTagGroup {
    private final String recordId;
    private final String name;

    private String type;
    private double order;

    private boolean searchable;
    private boolean browsable;
    private boolean predict;

    private double predictMinPercent;

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

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public boolean isBrowsable() {
        return browsable;
    }

    public void setBrowsable(boolean browsable) {
        this.browsable = browsable;
    }

    public boolean isPredict() {
        return predict;
    }

    public void setPredict(boolean predict) {
        this.predict = predict;
    }

    public double getPredictMinPercent() {
        return predictMinPercent;
    }

    public void setPredictMinPercent(double predictMinPercent) {
        this.predictMinPercent = predictMinPercent;
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
        PlaceTagGroup that = (PlaceTagGroup) o;
        return Objects.equals(recordId, that.recordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordId);
    }

    @Override
    public String toString() {
        return "PlaceTagGroup{" +
                "recordId='" + recordId + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", order=" + order +
                ", converts=" + converts +
                ", synonyms=" + synonyms +
                '}';
    }
}
