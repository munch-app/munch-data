package munch.data.place.collector;

import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 3:56 PM
 * Project: munch-data
 */
public class CollectedImage {
    public enum From {
        Place,
        Instagram,
        Article
    }

    private From from;
    private String uniqueId;

    private String source;
    private String sourceId;
    private Map<String, String> images;

    public From getFrom() {
        return from;
    }

    public void setFrom(From from) {
        this.from = from;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public Map<String, String> getImages() {
        return images;
    }

    public void setImages(Map<String, String> images) {
        this.images = images;
    }

    @Override
    public String toString() {
        return "CollectedImage{" +
                "from=" + from +
                ", uniqueId='" + uniqueId + '\'' +
                ", source='" + source + '\'' +
                ", sourceId='" + sourceId + '\'' +
                ", images=" + images +
                '}';
    }
}
