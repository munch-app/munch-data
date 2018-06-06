package munch.data;

import com.google.common.collect.Iterators;
import corpus.engine.AbstractEngine;
import munch.data.place.Place;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
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
public final class PlaceBridge extends AbstractEngine<Object> {
    private static final Logger logger = LoggerFactory.getLogger(PlaceBridge.class);

    private final munch.data.client.PlaceClient newClient;
    private final munch.data.clients.PlaceClient oldClient;

    @Inject
    public PlaceBridge(munch.data.client.PlaceClient newClient, munch.data.clients.PlaceClient oldClient) {
        super(logger);
        this.newClient = newClient;
        this.oldClient = oldClient;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(24);
    }

    @Override
    protected Iterator<Object> fetch(long cycleNo) {
        return Iterators.concat(oldClient.list(), newClient.list());
    }

    @Override
    protected void process(long cycleNo, Object data, long processed) {
        if (data instanceof munch.data.structure.Place) {
            // From OLD
            newClient.put(convert((munch.data.structure.Place) data));
        } else {
            // From NEW
            Place place = (Place) data;
            // Don't exists anymore
            if (oldClient.get(place.getPlaceId()) == null) {
                newClient.delete(place.getPlaceId());
            }
        }
    }

    public munch.data.place.Place convert(munch.data.structure.Place old) {
        Place place = new Place();
        place.setPlaceId(old.getId());
        // TODO All Fields
        return place;
    }
}
