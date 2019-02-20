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

    public static void main(String[] args) {

        String latLngs = "1.315727, 103.893624\n" +
                "1.318771, 103.892621\n" +
                "1.336038821571187,103.8529415562754\n" +
                "1.387320, 103.869675\n" +
                "1.291472, 103.849670\n" +
                "1.327563,103.673446\n" +
                "1.380552, 103.760058\n" +
                "1.323421751438994,103.8540680628599\n" +
                "1.353862117733181,103.9397261600388\n" +
                "1.336886, 103.779440\n" +
                "1.433037, 103.841210\n" +
                "1.299239, 103.857828\n" +
                "1.305108, 103.851391\n" +
                "1.431447688311437,103.8285376099418\n" +
                "1.275907524503414,103.7914026605528\n" +
                "1.378626, 103.763355\n" +
                "1.342894, 103.953155\n" +
                "1.300211, 103.837284\n" +
                "1.26405,103.819236\n" +
                "1.359771, 103.767581\n" +
                "1.273913798571447,103.8079435674162\n" +
                "1.341259706009987,103.6974042544675\n" +
                "1.433538856799564,103.7797848999963\n" +
                "1.28785810256754,103.818402623449\n" +
                "1.318473, 103.863039\n" +
                "1.331892, 103.744772\n" +
                "1.325097, 103.851357\n" +
                "1.29974,103.859781";

        for (String latLng : latLngs.split("\n")) {
            System.out.println(createPolygon(latLng, 0.3));
        }
    }
}
