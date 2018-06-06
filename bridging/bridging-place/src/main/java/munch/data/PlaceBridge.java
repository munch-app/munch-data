package munch.data;

import corpus.engine.AbstractEngine;
import munch.data.client.PlaceClient;
import munch.data.place.Place;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;

/**
 * Created by: Fuxing
 * Date: 6/6/18
 * Time: 3:00 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceBridge extends AbstractEngine<Place> {
    private static final Logger logger = LoggerFactory.getLogger(PlaceBridge.class);


    private final PlaceClient placeClient;

    public PlaceBridge(PlaceClient placeClient) {
        super(logger);
        this.placeClient = placeClient;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(24);
    }

    @Override
    protected Iterator<Place> fetch(long cycleNo) {
        return null;
    }

    @Override
    protected void process(long cycleNo, Place data, long processed) {

    }
}
