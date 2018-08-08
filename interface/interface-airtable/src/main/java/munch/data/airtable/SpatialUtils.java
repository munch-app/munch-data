package munch.data.airtable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 5/6/18
 * Time: 12:23 PM
 * Project: munch-data
 */
public final class SpatialUtils {
    private static final WKTWriter writer = new WKTWriter();
    private static final WKTReader reader = new WKTReader();

    /**
     * @param latLng center
     * @param radius in km
     * @return List of Points in Polygon
     */
    public static List<String> createPolygonPoints(String latLng, double radius) {
        return toPoints(createPolygon(latLng, radius));
    }

    /**
     * @param latLng center
     * @param radius in km
     * @return Polygon
     */
    public static Polygon createPolygon(String latLng, double radius) {
        double distance = (1 / 110.54) * radius;

        GeometryFactory geometryFactory = new GeometryFactory();

        String[] split = latLng.split(",");

        Point point = geometryFactory.createPoint(
                new Coordinate(
                        Double.parseDouble(split[1]),
                        Double.parseDouble(split[0])
                )
        );
        return (Polygon) point.buffer(distance);
    }

    public static List<String> toPoints(Polygon polygon) {
        return Arrays.stream(polygon.getCoordinates())
                .map(c -> c.y + "," + c.x)
                .collect(Collectors.toList());
    }

    public static String toWKT(List<String> list) {
        GeometryFactory geometryFactory = new GeometryFactory();

        Polygon polygonFromCoordinates = geometryFactory.createPolygon(list.stream()
                .map(s -> {
                    String[] split = s.split(",");
                    return new Coordinate(
                            Double.parseDouble(split[1]),
                            Double.parseDouble(split[0])
                    );
                }).toArray(Coordinate[]::new));
        return writer.write(polygonFromCoordinates);
    }

    public static List<String> wktToPoints(String wkt) {
        if (StringUtils.isBlank(wkt)) return null;
        try {
            //noinspection ConstantConditions
            Polygon polygon = (Polygon) reader.read(wkt);
            return Arrays.stream(polygon.getCoordinates())
                    .map(c -> c.y + "," + c.x)
                    .collect(Collectors.toList());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
