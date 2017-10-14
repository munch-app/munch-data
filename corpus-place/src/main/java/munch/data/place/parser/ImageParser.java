package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import corpus.images.ImageCachedKey;
import munch.data.structure.Place;

import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 10:13 PM
 * Project: munch-data
 */
@Singleton
public final class ImageParser extends AbstractParser {
    private static final int MAX_SIZE = 10;

    /**
     * @param list list of corpus
     * @return List of Place.Image can be empty
     */
    public List<Place.Image> parse(List<CorpusData> list) {
        return filter(list, PlaceKey.image).stream()
                .map(field -> {
                    Map<String, String> images = ImageCachedKey.getImages(field);
                    String source = ImageCachedKey.getSource(field, null);
                    if (images == null || source == null) return null;

                    Place.Image image = new Place.Image();
                    image.setWeight(ImageCachedKey.getWeight(field, 1.0));
                    image.setSource(source);
                    image.setImages(images);
                    return image;
                })
                .filter(Objects::nonNull)
                .sorted((o1, o2) -> Double.compare(o2.getWeight(), o1.getWeight()))
                .limit(MAX_SIZE)
                .collect(Collectors.toList());
    }
}
