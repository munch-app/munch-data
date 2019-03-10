package munch.data.elastic.plugins;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import munch.data.elastic.ElasticObject;
import munch.restful.core.JsonUtils;

/**
 * Created by: Fuxing
 * Date: 2019-03-10
 * Time: 19:25
 * Project: munch-data
 */
public final class GeometryPlugin implements ElasticPlugin {

    @Override
    public void serialize(ElasticObject object, ObjectNode node) {
        JsonNode locationNode = node.path("location");
        if (locationNode.isMissingNode()) return;

        JsonNode points = locationNode.path("polygon").path("points");
        if (points.isMissingNode()) return;

        ObjectNode polygon = JsonUtils.createObjectNode();
        polygon.put("type", "polygon");
        polygon.set("coordinates", pointsAsCoordinates(points));
        ((ObjectNode) locationNode).set("geometry", polygon);
    }

    /**
     * @param points points in ["lat,lng", "lat,lng"]
     * @return coordinates in [[[lng,lat], [lng,lat]]]
     */
    private static ArrayNode pointsAsCoordinates(JsonNode points) {
        ArrayNode coordinates = JsonUtils.createArrayNode();
        for (JsonNode point : points) {
            String[] split = point.asText().split(",");
            double lat = Double.parseDouble(split[0].trim());
            double lng = Double.parseDouble(split[1].trim());
            coordinates.add(JsonUtils.createArrayNode().add(lng).add(lat));
        }
        // Results will be [[[lng,lat], [lng,lat]]]
        return JsonUtils.createArrayNode().add(coordinates);
    }
}
