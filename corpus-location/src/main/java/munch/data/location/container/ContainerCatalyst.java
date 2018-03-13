package munch.data.location.container;

import catalyst.utils.exception.ExceptionRetriable;
import catalyst.utils.exception.Retriable;
import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.field.PlaceKey;
import munch.data.clients.ContainerClient;
import munch.data.structure.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Created by: Fuxing
 * Date: 4/5/2017
 * Time: 4:39 PM
 * Project: munch-corpus
 */
@Singleton
public class ContainerCatalyst extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(ContainerCatalyst.class);
    private static final Retriable retriable = new ExceptionRetriable(4);

    private final ContainerClient containerClient;
    private PostalMatcher postalMatcher;
    private PolygonMatcher polygonMatcher;

    @Inject
    public ContainerCatalyst(ContainerClient containerClient) {
        super(logger);
        this.containerClient = containerClient;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(24);
    }

    @Override
    protected boolean preCycle(long cycleNo) {
        postalMatcher = new PostalMatcher();
        polygonMatcher = new PolygonMatcher();
        corpusClient.list("Sg.Munch.Location.Container").forEachRemaining(data -> {
            Container container = ContainerUtils.createContainer(data);
            if (container == null) {
                logger.info("Failed to Create Container: {}", data);
                counter.increment("Failure");
                return;
            }

            postalMatcher.put(data, container);
            polygonMatcher.put(data, container);
            counter.increment("Loaded PostalMatcher");
            sleep(10);
        });

        counter.print();
        return super.preCycle(cycleNo);
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return corpusClient.list("Sg.Munch.Place");
    }

    @Override
    protected void process(long cycleNo, CorpusData data, long processed) {
        String placeId = data.getCatalystId();
        String postal = PlaceKey.Location.postal.getValueOrThrow(data);

        postalMatcher.find(postal, placeId, cycleNo).forEach(containerPlace -> {
            corpusClient.put("Sg.Munch.Location.ContainerPlace", placeId, containerPlace);
            counter.increment("Matched PostalMatcher");
        });

        PlaceKey.Location.latLng.getLatLng(data).ifPresent(latLng -> {
            polygonMatcher.find(latLng, placeId, cycleNo).forEach(containerPlace -> {
                corpusClient.put("Sg.Munch.Location.ContainerPlace", placeId, containerPlace);
                counter.increment("Matched PostalMatcher");
            });
        });

        sleep(250);
        if (processed % 1000 == 0) logger.info("Processed {}", processed);
    }

    @Override
    protected void postCycle(long cycleNo) {
        super.postCycle(cycleNo);

        Consumer<Container> consumer = (Container container) -> {
            // Put, Delete will be done at preCycle
            retriable.loop(() -> {
                containerClient.put(container);
                counter.increment("Updated");
            });
            sleep(100);
        };

        postalMatcher.forEach(consumer);
        polygonMatcher.forEach(consumer);
    }

    @Override
    protected void deleteCycle(long cycleNo) {
        corpusClient.deleteBefore("Sg.Munch.Location.ContainerPlace", cycleNo);
        corpusClient.deleteBefore("Sg.Munch.ContainerPlace", cycleNo);
    }
}
