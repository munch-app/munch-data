package munch.data.structure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;
import java.util.Objects;

/**
 * Technically this is a smaller subclass of ImageMeta in munch-images
 * with lesser fields
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SourcedImage {
    private String source;
    private String sourceId;
    private String sourceName;
    private Map<String, String> images;

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

    /**
     * different types of images
     *
     * @return type->url
     */
    public Map<String, String> getImages() {
        return images;
    }

    public void setImages(Map<String, String> images) {
        this.images = images;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourcedImage that = (SourcedImage) o;
        return Objects.equals(source, that.source) &&
                Objects.equals(images, that.images);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, images);
    }

    @Override
    public String toString() {
        return "SourcedImage{" +
                "source='" + source + '\'' +
                ", images=" + images +
                '}';
    }
}
