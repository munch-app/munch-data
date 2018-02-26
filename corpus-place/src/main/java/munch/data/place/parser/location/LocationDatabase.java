package munch.data.place.parser.location;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import corpus.data.CorpusClient;
import corpus.field.FieldUtils;
import munch.data.utils.ScheduledThreadUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 30/8/2017
 * Time: 1:27 AM
 * Project: munch-corpus
 */
@Singleton
public final class LocationDatabase {
    private static final Logger logger = LoggerFactory.getLogger(LocationDatabase.class);

    private final CorpusClient corpusClient;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final WKTReader reader = new WKTReader();

    private List<LocationPolygon> locations = Collections.emptyList();

    @Inject
    public LocationDatabase(CorpusClient corpusClient) {
        this.corpusClient = corpusClient;
        sync();

        ScheduledThreadUtils.schedule(this::sync, 24, TimeUnit.HOURS);
    }

    /**
     * @param lat latitude of place
     * @param lng longitude of place
     * @return location tags of place
     */
    public Set<String> findTags(double lat, double lng) {
        Point point = geometryFactory.createPoint(new Coordinate(lng, lat));
        return locations.stream()
                .filter(polygon -> polygon.intersects(point))
                .map(LocationPolygon::getName)
                .collect(Collectors.toSet());
    }

    public String findLocation(double lat, double lng) {
        Point point = geometryFactory.createPoint(new Coordinate(lng, lat));
        return locations.stream()
                .filter(polygon -> polygon.intersects(point))
                .min(Comparator.comparingDouble(LocationPolygon::getArea))
                .map(LocationPolygon::getName)
                .orElse(null);
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

    private void sync() {
        List<LocationPolygon> locations = new ArrayList<>();
        corpusClient.list("Sg.MunchSheet.LocationPolygon").forEachRemaining(data -> {
            String name = FieldUtils.getValue(data, "LocationPolygon.name");
            String polygon = FieldUtils.getValue(data, "LocationPolygon.polygon");
            if (StringUtils.isAnyBlank(name, polygon)) return;

            try {
                locations.add(new LocationPolygon(name, parsePolygon(polygon)));
            } catch (ParseException e) {
                logger.info("Unable to parse polygon", e);
            }
        });
        this.locations = locations;
    }

    private static class LocationPolygon {
        private String name;
        private Polygon polygon;

        public LocationPolygon(String name, Polygon polygon) {
            this.name = name;
            this.polygon = polygon;
        }

        public String getName() {
            return name;
        }

        public double getArea() {
            return polygon.getArea();
        }

        public boolean intersects(Point point) {
            return polygon.intersects(point);
        }
    }
}
