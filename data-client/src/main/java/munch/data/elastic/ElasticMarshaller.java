package munch.data.elastic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Singleton;
import munch.data.structure.Container;
import munch.data.structure.Location;
import munch.data.structure.Place;
import munch.data.structure.Tag;
import munch.restful.core.JsonUtils;
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
    private static final ObjectMapper mapper = JsonUtils.objectMapper;

    /**
     * @param place place to serialize to json for elastic
     * @return serialized json
     */
    public ObjectNode serialize(Place place) {
        ObjectNode node = mapper.valueToTree(place);
        node.put("dataType", "Place");

        // Overrides
        node.put("createdDate", place.getCreatedDate().getTime());
        node.put("updatedDate", place.getUpdatedDate().getTime());

        // Suggest Field
        ArrayNode inputs = mapper.createArrayNode();
        inputs.add(place.getName());
        node.putObject("suggest")
                .put("weight", 10)
                .set("input", inputs);
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
    public ObjectNode serialize(Location location) {
        ObjectNode node = mapper.createObjectNode();
        node.put("dataType", "Location");

        // Root Node
        node.put("id", location.getId());
        node.put("name", location.getName());
        node.put("createdDate", location.getCreatedDate().getTime());
        node.put("updatedDate", location.getUpdatedDate().getTime());

        // Location Node
        node.putObject("location")
                //now at /location/
                .put("city", location.getCity())
                .put("country", location.getCountry())
                .put("latLng", location.getLatLng())
                //now at /location/polygon/
                .putObject("polygon")
                .put("type", "polygon")
                .set("coordinates", pointsAsCoordinates(location.getPoints()));


        // Suggest Field
        ArrayNode inputs = mapper.createArrayNode();
        inputs.add(location.getName());
        node.putObject("suggest")
                .put("weight", 100)
                .set("input", inputs);
        return node;
    }

    public ObjectNode serialize(Tag tag) {
        ObjectNode node = mapper.createObjectNode();
        node.put("dataType", "Tag");

        // Root Node
        node.put("id", tag.getId());
        node.put("name", tag.getName());

        // Suggest Field
        ArrayNode inputs = mapper.createArrayNode();
        inputs.add(tag.getName());
        node.putObject("suggest")
                .put("weight", 100)
                .set("input", inputs);
        return node;
    }

    public ObjectNode serialize(Container container) {
        ObjectNode node = mapper.createObjectNode();
        node.put("dataType", "Container");

        // Root Node
        node.put("id", container.getId());
        node.put("name", container.getName());
        node.put("type", container.getType());

        node.put("phone", container.getPhone());
        node.put("website", container.getWebsite());
        node.put("description", container.getDescription());

        node.put("ranking", container.getRanking());

        Container.Location location = container.getLocation();
        node.putObject("location")
                //now at /location/
                .put("address", location.getAddress())
                .put("street", location.getStreet())
                .put("city", location.getCity())
                .put("country", location.getCountry())
                .put("postal", location.getPostal())
                .put("latLng", location.getLatLng());

        // Suggest Field
        ArrayNode inputs = mapper.createArrayNode();
        inputs.add(container.getName());
        node.putObject("suggest")
                .put("weight", 100)
                .set("input", inputs);
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
            case "Container":
                return (T) deserializeContainer(source);
            default:
                return null;
        }
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
     * @param node json node
     * @return deserialized Location
     */
    public Location deserializeLocation(JsonNode node) {
        Location location = new Location();
        location.setId(node.get("id").asText());
        location.setName(node.get("name").asText());
        location.setCreatedDate(new Date(node.get("createdDate").asLong()));
        location.setUpdatedDate(new Date(node.get("updatedDate").asLong()));

        location.setCity(node.path("location").path("city").asText());
        location.setCountry(node.path("location").path("country").asText());
        location.setLatLng(node.path("location").path("latLng").asText());

        // points: { "type": "polygon", "coordinates": [[[lng, lat]]]}
        List<String> points = new ArrayList<>();
        for (JsonNode point : node.path("location").path("polygon").path("coordinates").path(0)) {
            points.add(point.get(1).asDouble() + "," + point.get(0).asDouble());
        }
        location.setPoints(points);
        return location;
    }

    /**
     * @param node json node
     * @return deserialized Tag
     */
    public Tag deserializeTag(JsonNode node) {
        Tag tag = new Tag();
        tag.setId(node.get("id").asText());
        tag.setName(node.get("name").asText());
        return tag;
    }

    /**
     * @param node json node
     * @return deserialized Container
     */
    public Container deserializeContainer(JsonNode node) {
        Container container = new Container();
        container.setId(node.get("id").asText());
        container.setName(node.get("name").asText());
        container.setType(node.get("type").asText());

        container.setPhone(node.path("phone").asText());
        container.setWebsite(node.path("website").asText());
        container.setDescription(node.path("description").asText());

        container.setRanking(node.path("ranking").asDouble());

        Container.Location location = new Container.Location();
        location.setAddress(node.path("location").path("address").asText());
        location.setStreet(node.path("location").path("street").asText());
        location.setCity(node.path("location").path("city").asText());
        location.setCountry(node.path("location").path("country").asText());
        location.setPostal(node.path("location").path("postal").asText());
        location.setLatLng(node.path("location").path("latLng").asText());
        container.setLocation(location);

        return container;
    }

    /**
     * @param points points in ["lat,lng", "lat,lng"]
     * @return coordinates in [[[lng,lat], [lng,lat]]]
     */
    private static ArrayNode pointsAsCoordinates(List<String> points) {
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
