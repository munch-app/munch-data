package munch.data.container.matcher;

import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.field.ContainerKey;
import corpus.field.PlaceKey;
import munch.data.container.MunchContainerKey;
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

    private PostalContainerMatcher postalContainerMatcher;

    @Inject
    public ContainerPlaceCatalyst() {
        super(logger);
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(24);
    }

    @Override
    protected boolean preCycle(long cycleNo) {
        postalContainerMatcher = new PostalContainerMatcher();
        corpusClient.list("Sg.Munch.Container").forEachRemaining(data -> {
            String sourceCorpusName = MunchContainerKey.sourceCorpusName.getValueOrThrow(data);
            String sourceCorpusKey = MunchContainerKey.sourceCorpusKey.getValueOrThrow(data);
            CorpusData sourceData = corpusClient.get(sourceCorpusName, sourceCorpusKey);

            // Get Container.matching = postal only
            if (ContainerKey.matching.getValueOrThrow(sourceData).equals("postal")) {
                postalContainerMatcher.put(sourceData);
                counter.increment("Loaded PostalMatcher");
            }
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

        postalContainerMatcher.find(postal, placeId, cycleNo).forEach(containerPlace -> {
            corpusClient.put("Sg.Munch.ContainerPlace", placeId, containerPlace);
            counter.increment("Matched PostalMatcher");
        });

        sleep(250);
        if (processed % 1000 == 0) logger.info("Processed {}", processed);
    }

    @Override
    protected void deleteCycle(long cycleNo) {
        corpusClient.deleteBefore("Sg.Munch.ContainerPlace", cycleNo);
    }
}
