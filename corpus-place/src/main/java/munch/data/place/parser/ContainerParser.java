package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.ContainerKey;
import corpus.images.ImageField;
import munch.data.structure.Place;
import munch.data.structure.SourcedImage;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 10/12/2017
 * Time: 12:28 PM
 * Project: munch-data
 */
@Singleton
public final class ContainerParser extends AbstractParser<List<Place.Container>> {

    @Override
    public List<Place.Container> parse(Place place, List<CorpusData> list) {
        List<CorpusData> containerPlaces = findAll(list, "Sg.Munch.ContainerPlace");

        return containerPlaces.stream().map(data -> {
            Place.Container container = new Place.Container();
            container.setId(ContainerKey.id.getValue(data));
            container.setType(ContainerKey.type.getValue(data));
            container.setName(ContainerKey.name.getValue(data));

            container.setImages(collectImages(data));

            //noinspection ConstantConditions
            container.setRanking(ContainerKey.ranking.getValueDouble(data, 0.0));
            return container;
        })
                .filter(container -> StringUtils.isNoneBlank(container.getId(), container.getName()))
                .sorted(Comparator.comparing(Place.Container::getId))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("Duplicates")
    private List<SourcedImage> collectImages(CorpusData sourceData) {
        return ContainerKey.image.getAll(sourceData).stream()
                .map(ImageField::new)
                .filter(field -> field.getImages() != null && field.getSource() != null)
                .map(field -> {
                    SourcedImage image = new SourcedImage();
                    image.setSource(field.getSource());
                    image.setImages(field.getImages());
                    return image;
                })
                .sorted(Comparator.comparing(SourcedImage::getSource)
                        .thenComparing(Comparator.comparingInt(o -> o.getImages().hashCode())))
                .limit(1)
                .collect(Collectors.toList());
    }
}
