package munch.data.container;

import catalyst.utils.exception.ExceptionRetriable;
import catalyst.utils.exception.Retriable;
import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.field.ContainerKey;
import corpus.images.ImageField;
import munch.data.clients.ContainerClient;
import munch.data.structure.Container;
import munch.data.structure.SourcedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 10/12/2017
 * Time: 9:49 AM
 * Project: munch-data
 */
@Singleton
public final class ContainerCorpus extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(ContainerCorpus.class);
    private static final Retriable retriable = new ExceptionRetriable(4);

    private static final long dataVersion = 22;

    private final ContainerClient containerClient;

    @Inject
    public ContainerCorpus(ContainerClient containerClient) {
        super(logger);
        this.containerClient = containerClient;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofMinutes(30);
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return corpusClient.list(corpusName);
    }

    @Override
    protected void process(long cycleNo, CorpusData munchData, long processed) {
        CorpusData sourceData = getSourceData(munchData);

        if (sourceData != null) {
            if (!MunchContainerKey.updatedDate.equal(munchData, sourceData.getUpdatedDate(), dataVersion)) {
                munchData.replace(MunchContainerKey.updatedDate, sourceData.getUpdatedDate().getTime() + dataVersion);
                Container container = createContainer(sourceData);
                retriable.loop(() -> {
                    if (container != null) {
                        containerClient.put(container);
                        corpusClient.put("Sg.Munch.Container", munchData.getCorpusKey(), munchData);
                        logger.info("Updated: {}", container);
                        counter.increment("Updated");
                    } else {
                        logger.warn("Sg.Munch.Container from {} validation failed", sourceData);
                        counter.increment("Failure");
                    }
                });
            }
        } else {
            retriable.loop(() -> {
                // To delete
                containerClient.delete(munchData.getCorpusKey());
                corpusClient.delete("Sg.Munch.Container", munchData.getCorpusKey());
                counter.increment("Deleted");
            });
        }

        // Sleep for 1 second every 5 processed
        sleep(200);
    }

    /**
     * @param data local persisted tracker
     * @return actual linked data
     */
    private CorpusData getSourceData(CorpusData data) {
        List<CorpusData> dataList = catalystClient.listCorpus(data.getCatalystId(),
                MunchContainerKey.sourceCorpusName.getValueOrThrow(data), 1, null, null);

        if (dataList.isEmpty()) return null;
        return dataList.get(0);
    }

    private Container createContainer(CorpusData sourceData) {
        if (!ContainerKey.id.has(sourceData)) return null;
        if (!ContainerKey.name.has(sourceData)) return null;
        if (!ContainerKey.type.has(sourceData)) return null;
        if (!ContainerKey.ranking.has(sourceData)) return null;
        if (!ContainerKey.Location.city.has(sourceData)) return null;
        if (!ContainerKey.Location.country.has(sourceData)) return null;
        if (!ContainerKey.Location.postal.has(sourceData)) return null;
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
                .filter(field -> field.getImages() != null && field.getSource() != null)
                .map(field -> {
                    SourcedImage image = new SourcedImage();
                    image.setSource(field.getSource());
                    image.setImages(field.getImages());
                    return image;
                })
                .sorted(Comparator.comparingDouble(SourcedImage::getWeight).reversed()
                        .thenComparing(SourcedImage::getSource)
                        .thenComparing(Comparator.comparingInt(o -> o.getImages().hashCode())))
                .limit(5)
                .collect(Collectors.toList());
    }
}
