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
                Objects.equals(thumbnail, menu.thumbnail) &&
                Objects.equals(url, menu.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sortKey, type, thumbnail, url);
    }
}
