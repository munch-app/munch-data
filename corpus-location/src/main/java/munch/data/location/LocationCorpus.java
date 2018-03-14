package munch.data.location;

import catalyst.utils.LatLngUtils;
import com.google.common.collect.Iterators;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableMapper;
import corpus.data.CorpusData;
import corpus.engine.CorpusEngine;
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
 * Date: 13/3/18
 * Time: 9:54 PM
 * Project: munch-data
 */
@Singleton
public final class LocationCorpus extends CorpusEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(LocationCorpus.class);

    private final AirtableMapper mapper;
    private final WKTReader reader = new WKTReader();
    private final LocationClient locationClient;

    @Inject
    public LocationCorpus(AirtableApi airtableApi, LocationClient locationClient) {
        super(logger);
        this.locationClient = locationClient;
        AirtableApi.Table table = airtableApi.base("appbCCXympYqlVyvU").table("Polygon");
        this.mapper = new AirtableMapper(table);
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(12);
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return Iterators.transform(mapper.select(), record -> {
            CorpusData data = new CorpusData("Sg.Munch.Location.Polygon", record.getId(), cycleNo);
            data.setFields(record.getFields());

            try {
                return parse(data);
            } catch (ParseException e) {
                logger.warn("Polygon pattern parse error for row: {}", record, e);
            } catch (LatLngUtils.ParseException e) {
                logger.warn("Unable to latLng for row: {}", record, e);
            }
            return null;
        });
    }

    @Override
    protected void process(long cycleNo, CorpusData data, long processed) {
        super.process(cycleNo, data, processed);
        locationClient.put(createLocation(data));
    }

    @Override
    protected void deleteCycle(long cycleNo) {
        corpusClient.listBefore("Sg.Munch.Location.Polygon", cycleNo).forEachRemaining(data -> {
            locationClient.delete(data.getCorpusKey());
            corpusClient.delete("Sg.Munch.Location.Polygon", data.getCorpusKey());
            counter.increment("Deleted");
        });
    }

    private Location createLocation(CorpusData data) {
        Location location = new Location();
        location.setId(data.getCorpusKey());

        location.setName(LocationKey.name.getValueOrThrow(data));
        location.setCity(LocationKey.city.getValueOrThrow(data));
        location.setCountry(LocationKey.country.getValueOrThrow(data));

        location.setPoints(LocationKey.polygon.get(data)
                .map(field -> mapToPoints(field.getValue()))
                .orElseThrow(NullPointerException::new));
        location.setLatLng(LocationKey.latLng.getValueOrThrow(data));

        location.setUpdatedDate(data.getUpdatedDate());
        location.setCreatedDate(data.getCreatedDate());
        return location;
    }

    private CorpusData parse(CorpusData data) throws ParseException {
        if (!LocationKey.name.has(data)) return null;
        if (!LocationKey.city.has(data)) return null;
        if (!LocationKey.country.has(data)) return null;
        if (!LocationKey.polygon.has(data)) return null;

        // Set LatLng
        Polygon polygon = parsePolygon(LocationKey.polygon.getValueOrThrow(data));
        LatLngUtils.LatLng latLng = parseCenter(polygon, LocationKey.latLng.getValue(data));
        data.replace(LocationKey.latLng, latLng.toString());

        return data;
    }

    /**
     * @param polygon polygon to parse
     * @return polygon if parsed successfully
     * @throws ParseException parse exception if failed
     */
    private Polygon parsePolygon(String polygon) throws ParseException {
        Geometry geometry = reader.read(polygon);
        if (geometry instanceof Polygon) return (Polygon) geometry;
        throw new ParseException("Not polygon");
    }

    /**
     * @param geometry geometry for implicit if cannot find
     * @param center   center for explicit data
     * @return LatLng
     */
    private LatLngUtils.LatLng parseCenter(Geometry geometry, String center) {
        LatLngUtils.LatLng latLng = LatLngUtils.parse(center);
        if (latLng != null) return latLng;

        // Else get centroid
        Point point = geometry.getCentroid();
        return new LatLngUtils.LatLng(point.getY(), point.getX());
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
