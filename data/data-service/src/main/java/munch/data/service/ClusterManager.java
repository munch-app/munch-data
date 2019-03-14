package munch.data.service;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import munch.data.elastic.*;
import munch.data.exception.ElasticException;
import munch.data.location.Area;
import munch.data.place.Place;
import munch.restful.core.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 4/6/18
 * Time: 4:57 PM
 * Project: munch-data
 */
@Singleton
public final class ClusterManager {
    private static final Logger logger = LoggerFactory.getLogger(ClusterManager.class);
    private static final int MAX_SIZE = 500;

    private final JestClient client;
    private final ElasticIndex elasticIndex;
    private final PersistenceMapping persistenceMapping;

    @Inject
    public ClusterManager(JestClient client, ElasticIndex elasticIndex, PersistenceMapping persistenceMapping) {
        this.client = client;
        this.elasticIndex = elasticIndex;
        this.persistenceMapping = persistenceMapping;
    }

    /**
     * @param area used to search for Place and update all linked Place
     */
    public void update(Area area) {
        // TODO: Removed temporary due to the under-provisioned cluster
//        Objects.requireNonNull(area.getAreaId());
//        if (area.getType() != Area.Type.Cluster) return;
//
//        Area oldArea = elasticIndex.get(DataType.Area, area.getAreaId());
//        if (!isPolygonUpdated(oldArea, area)) return;

        // Link up Places in Area
        // Start completable future to persist Place
//        logger.info("Started Area Updating, areaId: {}", area.getAreaId());
//        int mutated = 0;
//        for (Place place : searchPlaces(area)) {
//            if (contains(place, area)) continue;
//
//            mutated++;
//            place.getAreas().add(area);
//            elasticIndex.put(place);
//            persistenceMapping.getMapping(place.getDataType())
//                    .getTable()
//                    .putItem(Item.fromJSON(JsonUtils.toString(place)));
//        }
//
//        logger.info("Completed Area Updating, mutated: {}, areaId: {}", mutated, area.getAreaId());
    }

    /**
     * @param area used to search for Place and delete all linked Place
     */
    public void delete(Area area) {
        if (area.getType() != Area.Type.Cluster) return;
        Objects.requireNonNull(area.getDataType());
        Objects.requireNonNull(area.getAreaId());

        // Iterate and remove Area from Place
        logger.info("Started Area Deleting, areaId: {}", area.getAreaId());
        int mutated = 0;
        for (Place place : searchPlaces(area.getAreaId())) {

            mutated++;
            place.getAreas().removeIf(area1 -> area1.getAreaId().equals(area.getAreaId()));
            elasticIndex.put(place);
            persistenceMapping.getMapping(place.getDataType())
                    .getTable()
                    .putItem(Item.fromJSON(JsonUtils.toString(place)));
        }

        logger.info("Completed Area Deleting, mutated: {}, areaId: {}", mutated, area.getAreaId());
    }

    /**
     * @param place to search for Areas and Update to Place
     */
    public void update(Place place) {
        place.setAreas(searchAreas(place));
        if (place.getAreas().isEmpty()) return;

        Set<String> names = new HashSet<>(place.getNames());
        place.getAreas().forEach(area -> {
            // Add Place + Area names
            names.add(place.getName() + " " + area.getName());
        });
        place.setNames(names);
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
        places.removeIf(place -> placeNotInArea(place, area));
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
                .add(ElasticUtils.filterIntersectsPoint("location.geometry", place.getLocation().getLatLng()))
        );
        root.putObject("query").set("bool", bool);

        List<Area> areaList = search(root);
        areaList.removeIf(area -> placeNotInArea(place, area));
        return areaList;
    }

    /**
     * @param area  to match
     * @param place to match
     * @return whether Area LocationCondition and Place condition match
     */
    private boolean placeNotInArea(Place place, Area area) {
        Area.LocationCondition condition = area.getLocationCondition();
        if (condition == null) return false;

        // Condition no PostCodes = ignore
        Set<String> postcodes = condition.getPostcodes();
        if (postcodes != null && !postcodes.isEmpty()) {
            // If Condition fail = false
            if (!postcodes.contains(place.getLocation().getPostcode())) return true;
        }

        // Condition no UnitNumbers = ignore
        Set<String> unitNumbers = condition.getUnitNumbers();
        if (unitNumbers != null && !unitNumbers.isEmpty()) {
            // If Condition fail = false
            if (!unitNumbers.contains(place.getLocation().getUnitNumber())) return true;
        }

        // Else Pass
        return false;
    }

    private <T extends ElasticObject> List<T> search(JsonNode query) {
        Search search = new Search.Builder(JsonUtils.toString(query))
                .addIndex(ElasticMapping.INDEX_NAME)
                .build();

        try {
            String json = client.execute(search).getJsonString();
            return ElasticUtils.deserializeList(JsonUtils.readTree(json).path("hits").path("hits"));
        } catch (IOException e) {
            throw ElasticException.parse(e);
        }
    }

    /**
     * @param oldArea old area
     * @param area    new area
     * @return if polygon is updated
     */
    private boolean isPolygonUpdated(Area oldArea, Area area) {
        Objects.requireNonNull(area);

        if (oldArea == null) return true;
        // Polygon Points should never be null because it is validated in AreaBridge
        return oldArea.getLocation().getPolygon().getPoints().equals(area.getLocation().getPolygon().getPoints());
    }

    private boolean contains(Place place, Area area) {
        if (place.getAreas().isEmpty()) return false;
        for (Area placeArea : place.getAreas()) {
            if (placeArea.getAreaId().equals(area.getAreaId())) return true;
        }
        return false;
    }
}
