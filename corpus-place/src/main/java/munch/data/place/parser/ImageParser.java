package munch.data.place.parser;

import com.google.common.collect.ImmutableSet;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import corpus.images.ImageCachedField;
import munch.data.structure.Place;

import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 10:13 PM
 * Project: munch-data
 */
@Singleton
public final class ImageParser extends AbstractParser<List<Place.Image>> {
    private static final int MAX_SIZE = 10;
    private static final Set<String> ARTICLE_SOURCE_IDS = ImmutableSet.of(
            "danielfooddiary.com", "sethlui.com", "ladyironchef.com",
            "misstamchiak.com", "sgfoodonfoot.com", "camemberu.com");

    /**
     * @param list list of corpus
     * @return List of Place.Image can be empty
     */
    @Override
    public List<Place.Image> parse(Place place, List<CorpusData> list) {
        return collect(list, PlaceKey.image).stream()
                .map(ImageCachedField::new)
                .filter(field -> field.getImages() != null && field.getSource() != null)
                .filter(field -> {
                    if (field.getSource().equals("facebook")) return true;
                    if (field.getSource().equals("article")) {
                        return ARTICLE_SOURCE_IDS.contains(field.getSourceId());
                    }
                    return false;
                })
                .map(field -> {
                    Place.Image image = new Place.Image();
                    image.setWeight(field.getWeight(1.0));
                    image.setSource(field.getSource());
                    image.setImages(field.getImages());
                    return image;
                })
                .filter(Objects::nonNull)
                .sorted((o1, o2) -> Double.compare(o2.getWeight(), o1.getWeight()))
                .limit(MAX_SIZE)
                .collect(Collectors.toList());
    }
}
