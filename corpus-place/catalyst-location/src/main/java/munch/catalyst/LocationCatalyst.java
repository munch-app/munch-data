package munch.catalyst;

import catalyst.ReactiveEngine;
import catalyst.data.CatalystClient;
import catalyst.data.DataClient;
import catalyst.utils.FieldCollector;
import catalyst.utils.LatLngUtils;
import catalyst.utils.exception.DateCompareUtils;
import com.google.inject.Inject;
import corpus.data.CorpusData;
import corpus.field.AbstractKey;
import corpus.field.NativeKey;
import corpus.field.PlaceKey;
import munch.catalyst.geocode.OneMapApi;
import munch.catalyst.street.StreetNameClient;
import munch.catalyst.tag.LocationDatabase;
import munch.catalyst.train.TrainDatabase;
import munch.corpus.docs.SheetNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Duration;
import java.util.Iterator;

/**
 * Created by: Fuxing
 * Date: 4/5/2017
 * Time: 10:07 PM
 * Project: munch-corpus
 */
public final class LocationCatalyst extends ReactiveEngine {
    private static final Logger logger = LoggerFactory.getLogger(LocationCatalyst.class);

    private final TrainDatabase trainDatabase; // With latLng
    private final LocationDatabase locationDatabase; // With latLng
    private final StreetNameClient streetNameClient; // With latLng
    private final OneMapApi oneMapApi;

    @Inject
    public LocationCatalyst(DataClient dataClient, CatalystClient catalystClient, TrainDatabase trainDatabase,
                            LocationDatabase locationDatabase, StreetNameClient streetNameClient, OneMapApi oneMapApi) {
        super(logger, dataClient, catalystClient);
        this.trainDatabase = trainDatabase;
        this.locationDatabase = locationDatabase;
        this.streetNameClient = streetNameClient;
        this.oneMapApi = oneMapApi;
    }

    @Override
    protected void preStart() {
        super.preStart();
        try {
            trainDatabase.sync();
            locationDatabase.sync();
        } catch (IOException | SheetNotFound e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected CorpusData process(String catalystId, @Nullable CorpusData data) {
        Collector collector = createCollector(dataClient.getLinked(catalystId));
        // Data and Collector = no data
        if (data == null && collector == null) return null;
        // Remove data from database
        if (data != null && collector == null) {
            catalystClient.delete(data);
            return null;
        }

        // If already exist don't keep finding wait for extended deadline
        if (data != null && collector.isPostal(data)) {
            // Refresh once every 5 days
            if (!DateCompareUtils.after(data.getCreatedDate(), Duration.ofDays(5))) {
                return null;
            }
        }

        return collector.collect(catalystId);
    }

    private Collector createCollector(Iterator<CorpusData> iterator) {
        // Place type and postal must exist
        FieldCollector typeCollector = new FieldCollector(PlaceKey.type, NativeKey.type);
        FieldCollector postalCollector = new FieldCollector(PlaceKey.Location.postal);
        iterator.forEachRemaining(corpusData -> {
            typeCollector.add(corpusData);
            postalCollector.add(corpusData);
        });

        // Must contain place type
        if (!typeCollector.collect().contains("place")) return null;

        String postal = postalCollector.collectMax();
        LatLngUtils.LatLng latLng = oneMapApi.geocode(postal);
        if (latLng == null) return null;

        return new Collector(postal, latLng);
    }

    private class Collector {
        private final String postal;
        private final LatLngUtils.LatLng latLng;

        private final AbstractKey NearestTrain = AbstractKey.of("Munch.Place.Location.nearestTrain");
        private final AbstractKey Street = AbstractKey.of("Munch.Place.Location.street");

        private Collector(String postal, LatLngUtils.LatLng latLng) {
            this.postal = postal;
            this.latLng = latLng;
        }

        boolean isPostal(CorpusData data) {
            return PlaceKey.Location.postal.get(data)
                    .map(CorpusData.Field::getValue)
                    .filter(value -> value.equals(postal))
                    .isPresent();
        }

        CorpusData collect(String catalystId) {
            CorpusData data = newCorpusData(catalystId);
            data.put(PlaceKey.Location.postal, postal);
            data.put(PlaceKey.Location.latLng, latLng.toString());

            data.put(NearestTrain, trainDatabase.findNearest(latLng.toString()).getName());
            data.put(Street, streetNameClient.getStreet(latLng.getLat(), latLng.getLng()));

            // Location tags
            locationDatabase.findTags(latLng.getLat(), latLng.getLng()).forEach(tag -> {
                data.put(PlaceKey.tag, tag);
            });
            return data;
        }
    }
}
