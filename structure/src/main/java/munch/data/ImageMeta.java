package munch.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * This is like a image group, a single image group has multiple types of same images
 * <p>
 * Created by: Fuxing
 * Date: 18/4/2017
 * Time: 10:10 PM
 * Project: munch-core
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ImageMeta {
    private String key;
    private Map<String, String> images;

    /**
     * @return unique id of the image
     */
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
    public String toString() {
        return "ImageMeta{" +
                "key='" + key + '\'' +
                ", images=" + images +
                '}';
    }
}
