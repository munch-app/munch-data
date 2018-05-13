package munch.data.location.container;

import corpus.data.CorpusData;
import corpus.field.ContainerKey;
import corpus.images.ImageField;
import munch.data.hour.HourExtractor;
import munch.data.hour.HourNormaliser;
import munch.data.hour.OpenHour;
import munch.data.structure.Container;
import munch.data.structure.SourcedImage;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 10/12/2017
 * Time: 11:27 AM
 * Project: munch-data
 */
public final class ContainerUtils {
    private static final HourExtractor HOUR_EXTRACTOR = new HourExtractor();
    private static final HourNormaliser HOUR_NORMALISER = new HourNormaliser();

    public static List<Container.Hour> getHours(CorpusData data) {
        String value = ContainerKey.hours.getValue(data);
        if (StringUtils.isBlank(value)) return List.of();

        List<OpenHour> hours = HOUR_EXTRACTOR.extract(value);
        List<OpenHour> normalised = HOUR_NORMALISER.normalise(hours);

        return normalised.stream()
                .map(openHour -> {
                    Container.Hour hour = new Container.Hour();
                    hour.setDay(openHour.getDay().name());
                    hour.setOpen(openHour.getOpen());
                    hour.setClose(openHour.getClose());
                    return hour;
                }).collect(Collectors.toList());
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
        container.setHours(getHours(sourceData));

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
