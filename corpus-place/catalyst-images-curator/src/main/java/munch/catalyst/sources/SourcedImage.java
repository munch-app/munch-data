package munch.catalyst.sources;

import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 6/8/2017
 * Time: 6:08 PM
 * Project: munch-corpus
 */
public final class SourcedImage {
    private final String source;
    private final String uniqueId;

    private final String imageKey;
    private final Map<String, String> images; // (Size -> Image Url)
    private final double boost;

    SourcedImage(String source, String uniqueId, String imageKey, Map<String, String> images, double boost) {
        this.source = source;
        this.imageKey = imageKey;
        this.images = images;
        this.boost = boost;
        this.uniqueId = uniqueId;
    }

    public String getSource() {
        return source;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getImageKey() {
        return imageKey;
    }

    public Map<String, String> getImages() {
        return images;
    }

    public double getBoost() {
        return boost;
    }
}
