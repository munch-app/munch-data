package munch.data.container;

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

/**
 * Created by: Fuxing
 * Date: 4/5/2017
 * Time: 4:39 PM
 * Project: munch-corpus
 */
@Singleton
public class ContainerPlaceCatalyst extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(ContainerPlaceCatalyst.class);
    private static final Retriable retriable = new ExceptionRetriable(4);

    private final ContainerClient containerClient;
    private ContainerMatcher containerMatcher;

    @Inject
    public ContainerPlaceCatalyst(ContainerClient containerClient) {
        super(logger);
        this.containerClient = containerClient;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(24);
    }

    @Override
    protected boolean preCycle(long cycleNo) {
        containerMatcher = new ContainerMatcher();
        corpusClient.list("Sg.Munch.Container").forEachRemaining(data -> {
            CorpusData sourceData = getSourceData(data);
            if (sourceData == null) {
                // Remove
                retriable.loop(() -> {
                    containerClient.delete(data.getCorpusKey());
                    corpusClient.delete("Sg.Munch.Container", data.getCorpusKey());
                    counter.increment("Deleted");
                });
                return;
            }

            Container container = MunchContainerKey.createContainer(sourceData);
            if (container == null) {
                logger.info("Failed to Create Container: {}", sourceData);
                counter.increment("Failure");
                return;
            }
            containerMatcher.put(sourceData, container);
            counter.increment("Loaded PostalMatcher");
            sleep(10);
        });

        counter.print();
        return super.preCycle(cycleNo);
    }

    /**
     * @param data local persisted tracker
     * @return CorpusData
     */
    private CorpusData getSourceData(CorpusData data) {
        String sourceCorpusName = MunchContainerKey.sourceCorpusName.getValueOrThrow(data);
        String sourceCorpusKey = MunchContainerKey.sourceCorpusKey.getValueOrThrow(data);
        return corpusClient.get(sourceCorpusName, sourceCorpusKey);
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return corpusClient.list("Sg.Munch.Place");
    }

    @Override
    protected void process(long cycleNo, CorpusData data, long processed) {
        String placeId = data.getCatalystId();
        String postal = PlaceKey.Location.postal.getValueOrThrow(data);

        containerMatcher.find(postal, placeId, cycleNo).forEach(containerPlace -> {
            corpusClient.put("Sg.Munch.ContainerPlace", placeId, containerPlace);
            counter.increment("Matched PostalMatcher");
        });

        sleep(250);
        if (processed % 1000 == 0) logger.info("Processed {}", processed);
    }

    @Override
    protected void postCycle(long cycleNo) {
        super.postCycle(cycleNo);

        containerMatcher.forEach(container -> {
            // Put, Delete will be done at preCycle
            retriable.loop(() -> {
                containerClient.put(container);
                counter.increment("Updated");
            });
            sleep(100);
        });
    }

    @Override
    protected void deleteCycle(long cycleNo) {
        super.deleteCycle(cycleNo);
        corpusClient.deleteBefore("Sg.Munch.ContainerPlace", cycleNo);
    }
}
