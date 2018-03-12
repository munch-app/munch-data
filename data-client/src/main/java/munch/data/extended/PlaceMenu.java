package munch.data.extended;

import java.util.Map;
import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 6/3/2018
 * Time: 12:35 AM
 * Project: munch-data
 */
public final class PlaceMenu implements ExtendedData {
    public static final String TYPE_IMAGE = "image";

    private String sortKey;
    private String type;

    private String source;
    private String sourceId;
    private String sourceName;
    private String sourceUrl;

    private String sourceContentTitle;
    private String sourceContentUrl;

    private Map<String, String> thumbnail;
    private String url;

    /**
     * @return sort key, order of PlaceMenu
     */
    @Override
    public String getSortKey() {
        return sortKey;
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    /**
     * @return type of place Menu
     * @see PlaceMenu#TYPE_IMAGE
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
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

    /**
     * @return thumbnail of Image
     */
    public Map<String, String> getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Map<String, String> thumbnail) {
        this.thumbnail = thumbnail;
    }

    /**
     * @return url destination of the menu
     */
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(ExtendedData data) {
        return equals((Object) data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaceMenu menu = (PlaceMenu) o;
        return Objects.equals(sortKey, menu.sortKey) &&
                Objects.equals(type, menu.type) &&
                Objects.equals(source, menu.source) &&
                Objects.equals(sourceName, menu.sourceName) &&
                Objects.equals(sourceId, menu.sourceId) &&
                Objects.equals(thumbnail, menu.thumbnail) &&
                Objects.equals(url, menu.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sortKey, type, source, sourceName, sourceId, thumbnail, url);
    }
}
