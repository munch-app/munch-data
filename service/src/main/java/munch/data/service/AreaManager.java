package munch.data.service;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import munch.data.ElasticObject;
import munch.data.elastic.ElasticIndex;
import munch.data.elastic.ElasticMapping;
import munch.data.elastic.ElasticUtils;
import munch.data.exception.ElasticException;
import munch.data.location.Area;
import munch.data.place.Place;
import munch.restful.core.JsonUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 4/6/18
 * Time: 4:57 PM
 * Project: munch-data
 */
@Singleton
public final class AreaManager {
    private static final int MAX_SIZE = 500;

    private final JestClient client;
    private final ElasticIndex elasticIndex;
    private final PersistenceMapping persistenceMapping;

    @Inject
    public AreaManager(JestClient client, ElasticIndex elasticIndex, PersistenceMapping persistenceMapping) {
        this.client = client;
        this.elasticIndex = elasticIndex;
        this.persistenceMapping = persistenceMapping;
    }

    /**
     * @param area used to search for Place and update all linked Place
     */
    public void update(Area area) {
        Area oldArea = elasticIndex.get("Area", Objects.requireNonNull(area.getAreaId()));
        if (!isPolygonUpdated(oldArea, area)) return;


        // Link up Places in Area
        for (Place place : searchPlaces(area)) {
            for (Area existingArea : place.getAreas()) {
                if (existingArea.getAreaId().equals(area.getAreaId())) {
                    break;
                }
            }

            place.getAreas().add(area);
            elasticIndex.put(place);
            persistenceMapping.getMapping(place.getDataType())
                    .getTable()
                    .putItem(Item.fromJSON(JsonUtils.toString(place)));
        }
    }

    /**
     * @param area used to search for Place and delete all linked Place
     */
    public void delete(Area area) {
        Objects.requireNonNull(area.getDataType());
        Objects.requireNonNull(area.getAreaId());

        // Iterate and remove Area from Place
        for (Place place : searchPlaces(area.getAreaId())) {
            place.getAreas().removeIf(area1 -> area1.getAreaId().equals(area.getAreaId()));
            elasticIndex.put(place);
            persistenceMapping.getMapping(place.getDataType())
                    .getTable()
                    .putItem(Item.fromJSON(JsonUtils.toString(place)));
        }
    }

    /**
     * @param place to search for Areas and Update to Place
     */
    public void update(Place place) {
        place.setAreas(searchAreas(place));
    }

    private List<Place> searchPlaces(String areaId) {
        ObjectNode root = JsonUtils.createObjectNode();
        root.put("from", 0);
        root.put("size", MAX_SIZE);

        ObjectNode bool = JsonUtils.createObjectNode();
        bool.set("must", ElasticUtils.mustMatchAll());
        bool.set("filter", JsonUtils.createArrayNode()
                .add(ElasticUtils.filterTerm("dataType", "Place"))
                .add(ElasticUtils.filterTerms("areas.areaId", List.of(areaId)))
        );
        root.putObject("query").set("bool", bool);
        return search(root);
    }

    private List<Place> searchPlaces(Area area) {
        if (area.getLocation() == null) return List.of();
        if (area.getLocation().getPolygon() == null) return List.of();
        if (area.getLocation().getPolygon().getPoints() == null) return List.of();

        ObjectNode root = JsonUtils.createObjectNode();
        root.put("from", 0);
        root.put("size", MAX_SIZE);

        ObjectNode bool = JsonUtils.createObjectNode();
        bool.set("must", ElasticUtils.mustMatchAll());
        bool.set("filter", JsonUtils.createArrayNode()
                .add(ElasticUtils.filterTerm("dataType", "Place"))
                .add(ElasticUtils.filterPolygon("location.latLng", area.getLocation().getPolygon().getPoints()))
        );
        root.putObject("query").set("bool", bool);

        List<Place> places = search(root);
        places.removeIf(place -> !validate(area, place));
        return places;
    }

    private List<Area> searchAreas(Place place) {
        ObjectNode root = JsonUtils.createObjectNode();
        root.put("from", 0);
        root.put("size", 100);

        ObjectNode bool = JsonUtils.createObjectNode();
        bool.set("must", ElasticUtils.mustMatchAll());
        bool.set("filter", JsonUtils.createArrayNode()
                .add(ElasticUtils.filterTerm("dataType", "Area"))
                .add(ElasticUtils.filterTerm("type", Area.Type.Cluster.name()))
                .add(ElasticUtils.filterWithinPoint("location.polygon", place.getLocation().getLatLng()))
        );
        root.putObject("query").set("bool", bool);

        List<Area> areaList = search(root);
        areaList.removeIf(area -> !validate(area, place));
        return areaList;
    }

    /**
     * @param area  to match
     * @param place to match
     * @return whether Area LocationCondition and Place condition match
     */
    private boolean validate(Area area, Place place) {
        Area.LocationCondition condition = area.getLocationCondition();

        // Condition no PostCodes = ignore
        if (!condition.getPostcodes().isEmpty()) {
            // If Condition fail = false
            if (!condition.getPostcodes().contains(place.getLocation().getPostcode())) return false;
        }

        // Condition no UnitNumbers = ignore
        if (!condition.getUnitNumbers().isEmpty()) {
            // If Condition fail = false
            if (!condition.getUnitNumbers().contains(place.getLocation().getUnitNumber())) return false;
        }

        // Else Pass
        return true;
    }

    private <T extends ElasticObject> List<T> search(JsonNode query) {
        Search search = new Search.Builder(JsonUtils.toString(query))
                .addIndex(ElasticMapping.INDEX_NAME)
                .build();

        try {
            String json = client.execute(search).getJsonString();
            return ElasticUtils.deserializeList(JsonUtils.toTree(json).path("hits").path("hits"));
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }

    /**
     * @param oldArea old area
     * @param area    new area
     * @return if polygon is updated
     */
    public boolean isPolygonUpdated(Area oldArea, Area area) {
        Objects.requireNonNull(area);

        if (oldArea == null) return true;
        if (oldArea.getLocation() == null || area.getLocation() == null) return false;
        if (oldArea.getLocation().getPolygon() == null || area.getLocation().getPolygon() == null) return false;
        if (oldArea.getLocation().getPolygon().getPoints() == null || area.getLocation().getPolygon().getPoints() == null)
            return false;
        return oldArea.getLocation().getPolygon().getPoints().equals(area.getLocation().getPolygon().getPoints());
    }
}
