package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.images.ImageField;
import munch.data.structure.Place;
import munch.data.structure.SourcedImage;

import javax.inject.Singleton;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 10:13 PM
 * Project: munch-data
 */
@Singleton
public final class ImageParser extends AbstractParser<List<SourcedImage>> {

    /**
     * @param list list of corpus
     * @return List of Place.Image can be empty
     */
    @Override
    public List<SourcedImage> parse(Place place, List<CorpusData> list) {
        return collect(list);
    }

    private List<SourcedImage> collect(List<CorpusData> list) {
        return list.stream()
                .filter(data -> data.getCorpusName().equals("Sg.Munch.Place.Image"))
                .flatMap(data -> data.getFields().stream())
                .filter(field -> field.getKey().equals("Place.image"))
                .sorted(Comparator.comparingInt(field -> Integer.parseInt(field.getValue())))
                .map(ImageField::asImageField)
                .map(field -> {
                    SourcedImage image = new SourcedImage();
                    image.setSource(field.getSource());
                    image.setSourceId(field.getSourceId());
                    image.setSourceName(field.getSourceName());

                    image.setSourceContentTitle(field.getSourceContentTitle());
                    image.setSourceContentUrl(field.getSourceContentUrl());
                    image.setImages(field.getImages());
                    return image;
                }).collect(Collectors.toList());
    }
}
