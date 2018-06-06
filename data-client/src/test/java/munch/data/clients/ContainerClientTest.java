package munch.data.clients;

import catalyst.utils.LatLngUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.io.Resources;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import munch.restful.core.JsonUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 4/6/18
 * Time: 11:22 PM
 * Project: munch-data
 */
class ContainerClientTest {
    private static final WKTReader reader = new WKTReader();
    private static final WKTWriter writer = new WKTWriter();

    @Test
    void name() throws IOException {
        URL url = Resources.getResource("locations.json");
        String json = IOUtils.toString(url, "utf-8");

        JsonNode locations = JsonUtils.readTree(json);
        List<JsonNode> list = new ArrayList<>();
        for (JsonNode data : locations.path("data")) {
            ObjectNode object = JsonUtils.createObjectNode();
            object.put("location.city", "singapore");
            object.put("location.country", "SGP");

            object.put("name", Objects.requireNonNull(data.path("name").asText()));


            if (data.path("dataType").asText().equals("Container")) {
                object.put("type", "Cluster");
                if (Set.of("Shopping Mall", "Hawker").contains(data.path("type").asText())) {
                    object.put("locationCondition.postcodes", data.path("location").path("postal").asText());
                }

                object.put("website", data.path("website").asText());
                object.put("description", data.path("description").asText());

                object.put("location.address", data.path("location").path("address").asText());
                object.put("location.postcode", data.path("location").path("postal").asText());

                object.put("location.latLng", data.path("location").path("latLng").asText());

                List<String> hourList = new ArrayList<>();
                for (JsonNode hours : data.path("hours")) {
                    hourList.add(hours.path("day").asText()
                            + ": " + hours.path("open").asText()
                            + "-" + hours.path("close").asText());
                }
                if (!hourList.isEmpty()) object.put("hours", Joiner.on("\n").join(hourList));

                String url1 = getUrl(data);
                if (url1 != null) {
                    object.putArray("images")
                            .addObject()
                            .put("url", url1);
                }

            } else {
                object.put("location.latLng", data.path("latLng").asText());
                object.put("location.polygon", pointsToWKT(JsonUtils.toList(data.path("points"), String.class)));
                object.put("type", "Region");
            }

            list.add(object);
        }

        System.out.println(JsonUtils.toString(list));
    }

    private static String getUrl(JsonNode data) {
        JsonNode images = data.path("images").path(0).path("images");
        if (images.has("original")) return images.path("original").asText();
        if (images.has("1080x1080")) return images.path("1080x1080").asText();
        if (images.has("640x640")) return images.path("640x640").asText();
        if (images.has("320x320")) return images.path("320x320").asText();
        if (images.has("150x150")) return images.path("150x150").asText();
        return null;
    }

//    private static String pointToWKT(String latLng, double radius) {
//        GeometryFactory geometryFactory = new GeometryFactory();
//
//        String[] split = latLng.split(",");
//
//        Point point = geometryFactory.createPoint(
//                new Coordinate(
//                        Double.parseDouble(split[1]),
//                        Double.parseDouble(split[0])
//                )
//        );
//        Polygon p1 = (Polygon) point.buffer(radius);
//        return writer.write(p1);
//    }

    private static String pointsToWKT(List<String> list) {
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

    private static List<String> mapToPoints(String wkt) {
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

    private static Polygon read(String value) {
        try {
            return (Polygon) reader.read(value);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static LatLngUtils.LatLng parseCenter(Geometry geometry, String center) {
        LatLngUtils.LatLng latLng = LatLngUtils.parse(center);
        if (latLng != null) return latLng;

        // Else get centroid
        Point point = geometry.getCentroid();
        return new LatLngUtils.LatLng(point.getY(), point.getX());
    }
}