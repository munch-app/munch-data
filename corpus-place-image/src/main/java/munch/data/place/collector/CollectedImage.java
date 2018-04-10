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
        ArticleFullPage, // Single article talking about a single place
        ArticleFullPageDoc, // Single article talking about a single place extract from docs
        ArticleListPage  // Single article talking about multiple place
    }

    private From from;
    private String imageKey;

    private String source;
    private String sourceId;
    private String sourceName;
    private String sourceUrl;

    private String sourceContentTitle;
    private String sourceContentUrl;
    private Map<String, String> images;

    public From getFrom() {
        return from;
    }

    public void setFrom(From from) {
        this.from = from;
    }

    public String getImageKey() {
        return imageKey;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
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

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getSourceContentTitle() {
        return sourceContentTitle;
    }

    public void setSourceContentTitle(String sourceContentTitle) {
        this.sourceContentTitle = sourceContentTitle;
    }

    public String getSourceContentUrl() {
        return sourceContentUrl;
    }

    public void setSourceContentUrl(String sourceContentUrl) {
        this.sourceContentUrl = sourceContentUrl;
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
                ", imageKey='" + imageKey + '\'' +
                ", source='" + source + '\'' +
                ", sourceId='" + sourceId + '\'' +
                ", sourceName='" + sourceName + '\'' +
                ", images=" + images +
                '}';
    }
}
