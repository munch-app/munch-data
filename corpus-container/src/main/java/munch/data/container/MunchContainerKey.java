package munch.data.container;

import corpus.data.CorpusData;
import corpus.field.AbstractKey;
import corpus.field.ContainerKey;
import corpus.images.ImageField;
import munch.data.structure.Container;
import munch.data.structure.SourcedImage;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 10/12/2017
 * Time: 11:27 AM
 * Project: munch-data
 */
public final class MunchContainerKey extends AbstractKey {
    public static final MunchContainerKey sourceCorpusName = new MunchContainerKey("sourceCorpusName", false);
    public static final MunchContainerKey sourceCorpusKey = new MunchContainerKey("sourceCorpusKey", false);

    private MunchContainerKey(String key, boolean multi) {
        super("Sg.Munch.Container." + key, multi);
    }

    public boolean equal(CorpusData data, Date date, long dataVersion) {
        String right = Long.toString(date.getTime() + dataVersion);
        return StringUtils.equals(getValueOrThrow(data), right);
    }

    public static Container createContainer(CorpusData sourceData) {
        if (sourceData == null) return null;

        if (!ContainerKey.id.has(sourceData)) return null;
        if (!ContainerKey.name.has(sourceData)) return null;
        if (!ContainerKey.type.has(sourceData)) return null;
        if (!ContainerKey.ranking.has(sourceData)) return null;
        if (!ContainerKey.Location.city.has(sourceData)) return null;
        if (!ContainerKey.Location.country.has(sourceData)) return null;
        if (!ContainerKey.Location.latLng.has(sourceData)) return null;

        Container container = new Container();
        container.setId(ContainerKey.id.getValue(sourceData));
        container.setName(ContainerKey.name.getValue(sourceData));
        container.setType(ContainerKey.type.getValue(sourceData));

        container.setPhone(ContainerKey.phone.getValue(sourceData));
        container.setWebsite(ContainerKey.website.getValue(sourceData));
        container.setDescription(ContainerKey.description.getValue(sourceData));

        Container.Location location = new Container.Location();
        location.setAddress(ContainerKey.Location.address.getValue(sourceData));
        location.setStreet(ContainerKey.Location.street.getValue(sourceData));

        location.setCity(ContainerKey.Location.city.getValue(sourceData));
        location.setCountry(ContainerKey.Location.country.getValue(sourceData));

        location.setPostal(ContainerKey.Location.postal.getValue(sourceData));
        location.setLatLng(ContainerKey.Location.latLng.getValue(sourceData));
        container.setLocation(location);

        container.setImages(collectImages(sourceData));

        //noinspection ConstantConditions
        container.setRanking(ContainerKey.ranking.getValueDouble(sourceData, 0.0));
        return container;
    }

    @SuppressWarnings("Duplicates")
    public static List<SourcedImage> collectImages(CorpusData sourceData) {
        return ContainerKey.image.getAll(sourceData).stream()
                .map(ImageField::new)
                .filter(field -> field.getImages() != null)
                .map(field -> {
                    SourcedImage image = new SourcedImage();
                    image.setSource(field.getSource());
                    image.setImages(field.getImages());
                    return image;
                })
                .sorted(Comparator.comparing(SourcedImage::getSource)
                        .thenComparing(Comparator.comparingInt(o -> o.getImages().hashCode())))
                .limit(5)
                .collect(Collectors.toList());
    }
}
