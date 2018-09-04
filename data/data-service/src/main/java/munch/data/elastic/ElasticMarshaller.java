package munch.data.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Singleton;
import munch.data.*;
import munch.restful.core.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 9/7/2017
 * Time: 1:00 AM
 * Project: munch-core
 */
@Singleton
public final class ElasticMarshaller {
    private static final Logger logger = LoggerFactory.getLogger(ElasticMarshaller.class);
    private static final ObjectMapper mapper = JsonUtils.objectMapper;

    public ObjectNode serialize(ElasticObject object) {
        ObjectNode node = mapper.valueToTree(object);
        serializeLocation(node.path("location"));

        // SuggestObject Field
        if (object instanceof SuggestObject) {
            Set<String> input = new HashSet<>();
            input.add(((SuggestObject) object).getName());

            if (object instanceof MultipleNameObject) {
                input.addAll(((MultipleNameObject) object).getNames());
            }

            node.putObject("suggest")
                    .put("weight", 1)
                    .set("input", JsonUtils.toTree(input));
        }

        if (object instanceof TimingObject) {
            List<Hour> hours = ((TimingObject) object).getHours();
            node.set("hour", parseHour(hours));
        }

        return node;
    }

    /**
     * @param results from es
     * @param <T>     deserialized type
     * @return deserialized type into a list
     */
    public <T extends ElasticObject> List<T> deserializeList(JsonNode results) {
        return ElasticUtils.deserializeList(results);
    }

    /**
     * @param node node to deserialize
     * @param <T>  deserialized type
     * @return deserialized type
     */
    @SuppressWarnings("unchecked")
    public <T extends ElasticObject> T deserialize(JsonNode node) {
        deserializeLocation(node.path("_source").path("location"));
        return ElasticUtils.deserialize(node);
    }

    private static void serializeLocation(JsonNode location) {
        if (location.isMissingNode()) return;

        JsonNode points = location.path("polygon").path("points");
        if (!points.isMissingNode()) {
            ObjectNode polygon = mapper.createObjectNode();
            polygon.put("type", "polygon");
            polygon.set("coordinates", pointsAsCoordinates(points));
            ((ObjectNode) location).set("polygon", polygon);
        }
    }

    private static void deserializeLocation(JsonNode location) {
        if (location.isMissingNode()) return;

        JsonNode polygon = location.path("polygon");
        if (!polygon.isMissingNode()) {
            ((ObjectNode) location)
                    .putObject("polygon")
                    .set("points", polygonAsPoints(polygon));
        }
    }

    /**
     * @param points points in ["lat,lng", "lat,lng"]
     * @return coordinates in [[[lng,lat], [lng,lat]]]
     */
    private static ArrayNode pointsAsCoordinates(JsonNode points) {
        ArrayNode coordinates = mapper.createArrayNode();
        for (JsonNode point : points) {
            String[] split = point.asText().split(",");
            double lat = Double.parseDouble(split[0].trim());
            double lng = Double.parseDouble(split[1].trim());
            coordinates.add(mapper.createArrayNode().add(lng).add(lat));
        }
        // Results will be [[[lng,lat], [lng,lat]]]
        return mapper.createArrayNode().add(coordinates);
    }

    private static ArrayNode polygonAsPoints(JsonNode polygon) {
        ArrayNode points = JsonUtils.createArrayNode();
        for (JsonNode point : polygon.path("coordinates").path(0)) {
            points.add(point.get(1).asDouble() + "," + point.get(0).asDouble());
        }
        return points;
    }

    private static ObjectNode parseHour(List<Hour> hours) {
        ObjectNode objectNode = mapper.createObjectNode();
        if (hours.isEmpty()) return objectNode;

        objectNode.set("mon", collectDay(Hour.Day.mon, hours));
        objectNode.set("tue", collectDay(Hour.Day.tue, hours));
        objectNode.set("wed", collectDay(Hour.Day.wed, hours));
        objectNode.set("thu", collectDay(Hour.Day.thu, hours));
        objectNode.set("fri", collectDay(Hour.Day.fri, hours));
        objectNode.set("sat", collectDay(Hour.Day.sat, hours));
        objectNode.set("sun", collectDay(Hour.Day.sun, hours));
        return objectNode;
    }

    private static ArrayNode collectDay(Hour.Day day, List<Hour> hours) {
        ArrayNode arrayNode = mapper.createArrayNode();

        for (Hour hour : hours) {
            if (day.equals(hour.getDay())) {
                try {
                    arrayNode.addObject()
                            .putObject("open_close")
                            .put("gte", serializeTime(hour.getOpen()))
                            .put("lte", serializeTime(hour.getClose()));
                } catch (NumberFormatException | NullPointerException | IndexOutOfBoundsException e) {
                    logger.error("Time parse error", e);
                }
            }
        }
        return arrayNode;
    }

    public static int serializeTime(String time) {
        String[] hourMin = time.split(":");
        return Integer.parseInt(hourMin[0]) * 60 + Integer.parseInt(hourMin[1]);
    }
}
