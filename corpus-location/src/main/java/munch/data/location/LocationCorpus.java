package munch.data.location;

import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.field.FieldUtils;
import munch.data.clients.LocationClient;
import munch.data.structure.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 10/10/2017
 * Time: 3:21 AM
 * Project: munch-data
 */
@Singleton
public class LocationCorpus extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(LocationCorpus.class);
    private static final WKTReader reader = new WKTReader();

    private final LocationClient locationClient;

    @Inject
    public LocationCorpus(LocationClient locationClient) {
        super(logger);
        this.locationClient = locationClient;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofMinutes(30);
    }

    @Override
    protected long loadCycleNo() {
        return System.currentTimeMillis();
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return corpusClient.list(corpusName);
    }

    @Override
    protected void process(long cycleNo, CorpusData data, long processed) {
        List<CorpusData> dataList = catalystClient.listCorpus(data.getCatalystId(),
                "Sg.MunchSheet.LocationPolygon", 1, null, null);
        if (!dataList.isEmpty()) {
            // To put if changed
            CorpusData locationPolygon = dataList.get(0);
            if (LocationKey.updatedDate.equal(data, locationPolygon.getUpdatedDate())) {
                locationClient.put(createLocation(locationPolygon));
            }
        } else {
            // To delete
            locationClient.delete(data.getCorpusKey());
            corpusClient.delete(corpusName, data.getCorpusKey());
        }

        // Sleep for 1 second every 5 processed
        if (processed % 5 == 0) {
            sleep(1000);
        }
    }

    private Location createLocation(CorpusData data) {
        Location location = new Location();
        location.setId(data.getCorpusKey());

        location.setName(FieldUtils.getValueOrThrow(data, "LocationPolygon.name"));
        location.setCity(FieldUtils.getValueOrThrow(data, "LocationPolygon.city"));
        location.setCountry(FieldUtils.getValueOrThrow(data, "LocationPolygon.country"));

        location.setPoints(FieldUtils.get(data, "LocationPolygon.polygon")
                .map(field -> mapToPoints(field.getValue()))
                .orElseThrow(NullPointerException::new));
        location.setLatLng(FieldUtils.getValueOrThrow(data, "LocationPolygon.latLng"));

        location.setUpdatedDate(data.getUpdatedDate());
        location.setCreatedDate(data.getCreatedDate());
        return location;
    }

    private List<String> mapToPoints(String wkt) {
        try {
            //noinspection ConstantConditions
            Polygon polygon = (Polygon) reader.read(wkt);
            return Arrays.stream(polygon.getCoordinates())
                    .map(c -> c.y + "," + c.x)
                    .collect(Collectors.toList());
        } catch (ParseException e) {
            logger.error("Unable to parse Place.Location.polygon WKT: {}", wkt, e);
            throw new RuntimeException(e);
        }
    }
}
