package munch.data.place.text;

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

    public abstract List<CollectedText> collect(String placeId, List<CorpusData> list);

    protected CollectedText mapField(CorpusData.Field field, CollectedText.From from) {
        ImageField imageField;
        if (field instanceof ImageField) {
            imageField = (ImageField) field;
        } else {
            imageField = new ImageField(field);
        }

        if (imageField.getImages() == null) return null;
        if (imageField.getImages().isEmpty()) return null;

        // Map CollectedImage
        CollectedText text = new CollectedText();
        text.setFrom(from);
        text.setUniqueId(getUniqueId(imageField));
        text.setContent(field.getValue());
        return text;
    }

    private static String getUniqueId(ImageField imageField) {
        Objects.requireNonNull(imageField.getImageKey());
        if (imageField.getSource() != null) {
            return imageField.getSource() + "|" + imageField.getImageKey();
        } else {
            Objects.requireNonNull(imageField.getBucket());
            return imageField.getBucket() + "|" + imageField.getImageKey();
        }
    }
}
