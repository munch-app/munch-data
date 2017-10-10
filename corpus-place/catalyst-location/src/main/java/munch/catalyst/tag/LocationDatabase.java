package munch.catalyst.tag;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import corpus.data.DataClient;
import corpus.field.PlaceKey;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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

    private final DataClient dataClient;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final WKTReader reader = new WKTReader();

    private List<LocationPolygon> polygons = Collections.emptyList();

    @Inject
    public LocationDatabase(DataClient dataClient) {
        this.dataClient = dataClient;
    }

    public void sync() {
        List<LocationPolygon> polygons = new ArrayList<>();
        dataClient.getBefore("Sg.Munch.LocationPolygon", Long.MAX_VALUE).forEachRemaining(data -> {
            String name = PlaceKey.name.getValue(data);
            String polygon = PlaceKey.Location.polygon.getValue(data);
            if (StringUtils.isAnyBlank(name, polygon)) return;

            try {
                polygons.add(new LocationPolygon(name, parsePolygon(polygon)));
            } catch (ParseException e) {
                logger.info("Unable to parse polygon", e);
            }
        });
        this.polygons = polygons;
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
     * @param lat latitude of place
     * @param lng longitude of place
     * @return location tags of place
     */
    public Set<String> findTags(double lat, double lng) {
        Point point = geometryFactory.createPoint(new Coordinate(lng, lat));
        return polygons.stream()
                .filter(polygon -> polygon.intersects(point))
                .map(LocationPolygon::getName)
                .collect(Collectors.toSet());
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

        public boolean intersects(Point point) {
            return polygon.intersects(point);
        }
    }
}
