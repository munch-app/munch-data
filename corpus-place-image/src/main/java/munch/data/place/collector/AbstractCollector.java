package munch.data.place.collector;

import corpus.data.CorpusData;
import corpus.images.ImageField;

import java.util.List;
import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 4:30 PM
 * Project: munch-data
 */
public abstract class AbstractCollector {

    public abstract List<CollectedImage> collect(String placeId, List<CorpusData> list);

    protected CollectedImage mapField(CorpusData.Field field, CollectedImage.From from) {
        ImageField imageField;
        if (field instanceof ImageField) {
            imageField = (ImageField) field;
        } else {
            imageField = new ImageField(field);
        }

        // Map CollectedImage
        CollectedImage image = new CollectedImage();
        image.setFrom(from);
        image.setUniqueId(getUniqueId(imageField));
        image.setSource(imageField.getSource());
        image.setSourceId(imageField.getSourceId());
        image.setImages(imageField.getImages());
        return image;
    }

    private static String getUniqueId(ImageField imageField) {
        Objects.requireNonNull(imageField.getSource());
        Objects.requireNonNull(imageField.getImageKey());
        return imageField.getSource() + "/" + imageField.getImageKey();
    }
}
