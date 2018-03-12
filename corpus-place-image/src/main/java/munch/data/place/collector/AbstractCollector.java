package munch.data.place.collector;

import corpus.data.CorpusData;
import corpus.images.ImageField;

import java.util.List;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 4:30 PM
 * Project: munch-data
 */
public abstract class AbstractCollector {

    public abstract List<CollectedImage> collect(String placeId, List<CorpusData> list);

    protected CollectedImage mapField(CorpusData.Field field, CollectedImage.From from) {
        ImageField imageField = ImageField.asImageField(field);

        // Required fields validation
        // Cannot validate sourceId and sourceName because some data source don't provide those
        if (imageField.getSource() == null) return null;
        if (imageField.getImageKey() == null) return null;
        if (imageField.getImages() == null) return null;
        if (imageField.getImages().isEmpty()) return null;

        // Map CollectedImage
        CollectedImage image = new CollectedImage();
        image.setFrom(from);
        image.setImageKey(imageField.getImageKey());

        image.setSource(imageField.getSource());
        image.setSourceId(imageField.getSourceId());
        image.setSourceName(imageField.getSourceName());
        image.setSourceUrl(imageField.getSourceUrl());

        image.setSourceContentTitle(imageField.getSourceContentTitle());
        image.setSourceContentUrl(imageField.getSourceContentUrl());

        image.setImages(imageField.getImages());
        return image;
    }
}
