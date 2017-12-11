package munch.data.place.parser;

import com.google.common.collect.ImmutableSet;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import corpus.images.ImageCachedField;
import munch.data.place.parser.images.ImagePlaceholderDatabase;
import munch.data.structure.Place;
import munch.data.structure.SourcedImage;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 10:13 PM
 * Project: munch-data
 */
@Singleton
public final class ImageParser extends AbstractParser<List<SourcedImage>> {
    private static final int MAX_SIZE = 10;
    private static final Set<String> ARTICLE_SOURCE_IDS = ImmutableSet.of(
            "danielfooddiary.com", "sethlui.com", "ladyironchef.com",
            "misstamchiak.com", "sgfoodonfoot.com", "camemberu.com",
            "ieatandeat.com", "aspirantsg.com", "ms-skinnyfat.com", "six-and-seven.com");

    private final ImagePlaceholderDatabase placeholderDatabase;

    @Inject
    public ImageParser(ImagePlaceholderDatabase placeholderDatabase) {
        this.placeholderDatabase = placeholderDatabase;
    }

    /**
     * @param list list of corpus
     * @return List of Place.Image can be empty
     */
    @Override
    public List<SourcedImage> parse(Place place, List<CorpusData> list) {
        List<SourcedImage> images = collectImages(list);
        if (!images.isEmpty()) return images;

        return placeholderDatabase.findImages(place.getTag());
    }

    @SuppressWarnings("Duplicates")
    private List<SourcedImage> collectImages(List<CorpusData> list) {
        return collect(list, PlaceKey.image).stream()
                .map(ImageCachedField::new)
                .filter(field -> field.getImages() != null && field.getSource() != null)
                .filter(field -> {
                    if (field.getSource().equals("facebook")) return true;
                    if (field.getSource().equals("munch-franchise")) return true;
                    if (field.getSource().equals("article")) {
                        return ARTICLE_SOURCE_IDS.contains(field.getSourceId());
                    }
                    return false;
                })
                .map(field -> {
                    SourcedImage image = new SourcedImage();
                    image.setWeight(field.getWeight(1.0));
                    image.setSource(field.getSource());
                    image.setImages(field.getImages());
                    return image;
                })
                .sorted(Comparator.comparingDouble(SourcedImage::getWeight).reversed()
                        .thenComparing(SourcedImage::getSource)
                        .thenComparing(Comparator.comparingInt(o -> o.getImages().hashCode())))
                .limit(MAX_SIZE)
                .collect(Collectors.toList());
    }
}
