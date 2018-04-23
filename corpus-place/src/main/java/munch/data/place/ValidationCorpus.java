package munch.data.place;

import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import munch.data.clients.PlaceClient;
import munch.data.elastic.ElasticIndex;
import munch.data.structure.Place;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;

/**
 * Created by: Fuxing
 * Date: 15/4/2018
 * Time: 4:56 AM
 * Project: munch-data
 */
@Singleton
public final class ValidationCorpus extends CatalystEngine<Place> {
    private static final Logger logger = LoggerFactory.getLogger(ValidationCorpus.class);

    private final PlaceClient placeClient;
    private final ElasticIndex elasticIndex;

    @Inject
    public ValidationCorpus(PlaceClient placeClient, ElasticIndex elasticIndex) {
        super(logger);
        this.placeClient = placeClient;
        this.elasticIndex = elasticIndex;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofDays(31);
    }

    @Override
    protected Iterator<Place> fetch(long cycleNo) {
        return elasticIndex.scroll("Place", "6m");
    }

    @Override
    protected void process(long cycleNo, Place data, long processed) {
        CorpusData corpusData = corpusClient.get("Sg.Munch.Place", data.getId());
        if (corpusData == null) {
            placeClient.delete(data.getId());
            logger.info("Deleted Place {}", data.getId());
        }

        sleep(100);
    }
}
