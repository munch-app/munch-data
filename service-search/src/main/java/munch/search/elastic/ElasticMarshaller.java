package munch.search.elastic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import munch.data.Location;
import munch.data.Place;
import munch.data.Tag;
import munch.restful.core.exception.JsonException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 9/7/2017
 * Time: 1:00 AM
 * Project: munch-core
 */
@Singleton
public final class ElasticMarshaller {
    private final ObjectMapper mapper;

    @Inject
    public ElasticMarshaller(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public ObjectNode serialize(Tag tag, long cycleNo) {
        ObjectNode node = mapper.createObjectNode();
        node.put("dataType", "Tag");
        node.put("cycleNo", cycleNo);

        // Root Node
        node.put("id", tag.getId());
        node.put("name", tag.getName());
        node.put("type", tag.getType());

        // Suggest Field
        ArrayNode suggest = mapper.createArrayNode();
        suggest.add(tag.getName());
        node.set("suggest", suggest);
        return node;
    }

    /**
     * If coordinates failed to parse, exception will be thrown
     *
     * @param location location to serialize to json for elastic
     * @return serialized json
     * @throws NullPointerException      if any points is null
     * @throws IndexOutOfBoundsException if points are not in the array
     * @throws NumberFormatException     if points are not double
     */
    public ObjectNode serialize(Location location, long cycleNo) {
        ObjectNode node = mapper.createObjectNode();
        node.put("dataType", "Location");
        node.put("cycleNo", cycleNo);

        // Root Node
        node.put("id", location.getId());
        node.put("name", location.getName());
        node.put("updatedDate", location.getUpdatedDate().getTime());

        // Location Node
        node.putObject("location")
                //now at /location/
                .put("city", location.getCity())
                .put("country", location.getCountry())
                .put("latLng", location.getCenter())
                //now at /location/polygon/
                .putObject("polygon")
                .put("type", "polygon")
                .set("coordinates", pointsAsCoordinates(location.getPoints()));


        // Suggest Field
        ArrayNode suggest = mapper.createArrayNode();
        suggest.add(location.getName());
        node.set("suggest", suggest);
        return node;
    }

    /**
     * @param place place to serialize to json for elastic
     * @return serialized json
     */
    public ObjectNode serialize(Place place, long cycleNo) {
        ObjectNode node = mapper.valueToTree(place);
        node.put("dataType", "Place");
        node.put("cycleNo", cycleNo);
        node.put("createdDate", place.getCreatedDate().getTime());
        node.put("updatedDate", place.getUpdatedDate().getTime());

        // Suggest Field
        ArrayNode suggest = mapper.createArrayNode();
        suggest.add(place.getName());
        node.set("suggest", suggest);
        return node;
    }

    /**
     * @param results results
     * @param <T>     deserialized type
     * @return deserialized type
     */
    public <T> List<T> deserializeList(JsonNode results) {
        if (results.isMissingNode()) return Collections.emptyList();
        List<T> list = new ArrayList<>();
        for (JsonNode result : results) list.add(deserialize(result));
        return list;
    }

    /**
     * @param node node to deserialize
     * @param <T>  deserialized type
     * @return deserialized type
     */
    @SuppressWarnings("unchecked")
    public <T> T deserialize(JsonNode node) {
        JsonNode source = node.path("_source");
        switch (source.path("dataType").asText()) {
            case "Location":
                return (T) deserializeLocation(source);
            case "Place":
                return (T) deserializePlace(source);
            case "Tag":
                return (T) deserializeTag(source);
            default:
                return null;
        }
    }

    /**
     * @param node json node
     * @return deserialized Tag
     */
    public Tag deserializeTag(JsonNode node) {
        Tag tag = new Tag();
        tag.setId(node.get("id").asText());
        tag.setName(node.get("name").asText());
        tag.setType(node.get("type").asText());
        return tag;
    }

    /**
     * @param node json node
     * @return deserialized Location
     */
    public Location deserializeLocation(JsonNode node) {
        Location location = new Location();
        location.setId(node.get("id").asText());
        location.setName(node.get("name").asText());
        location.setUpdatedDate(new Date(node.get("updatedDate").asLong()));

        location.setCity(node.at("/location/city").asText());
        location.setCountry(node.at("/location/country").asText());
        location.setCenter(node.at("/location/latLng").asText());

        // points: { "type": "polygon", "coordinates": [[[lng, lat]]]}
        List<String> points = new ArrayList<>();
        for (JsonNode point : node.at("/location/polygon/coordinates/0")) {
            points.add(point.get(1).asDouble() + "," + point.get(0).asDouble());
        }
        location.setPoints(points);
        return location;
    }

    /**
     * @param node json node
     * @return deserialized Place
     */
    public Place deserializePlace(JsonNode node) {
        try {
            return mapper.treeToValue(node, Place.class);
        } catch (JsonProcessingException e) {
            throw new JsonException(e);
        }
    }

    /**
     * @param points points in ["lat,lng", "lat,lng"]
     * @return coordinates in [[[lng,lat], [lng,lat]]]
     */
    private ArrayNode pointsAsCoordinates(List<String> points) {
        ArrayNode coordinates = mapper.createArrayNode();
        for (String point : points) {
            String[] split = point.split(",");
            double lat = Double.parseDouble(split[0].trim());
            double lng = Double.parseDouble(split[1].trim());
            coordinates.add(mapper.createArrayNode().add(lng).add(lat));
        }
        // Results will be [[[lng,lat], [lng,lat]]]
        return mapper.createArrayNode().add(coordinates);
    }
}
