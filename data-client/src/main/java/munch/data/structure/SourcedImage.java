package munch.data.structure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.math.DoubleMath;

import java.util.Map;
import java.util.Objects;

/**
 * Technically this is a smaller subclass of ImageMeta in munch-images
 * with lesser fields
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SourcedImage {
    private double weight;
    private String source;
    private Map<String, String> images;

    @Deprecated
    public double getWeight() {
        return weight;
    }

    @Deprecated
    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
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

        SourcedImage image = (SourcedImage) o;

        if (!DoubleMath.fuzzyEquals(image.weight, weight, 0.05)) return false;
        if (!source.equals(image.source)) return false;
        return images.equals(image.images);
    }

    @Override
    public int hashCode() {
        return Objects.hash(weight, source, images);
    }

    @Override
    public String toString() {
        return "Image{" +
                "weight=" + weight +
                ", source='" + source + '\'' +
                ", images=" + images +
                '}';
    }
}
