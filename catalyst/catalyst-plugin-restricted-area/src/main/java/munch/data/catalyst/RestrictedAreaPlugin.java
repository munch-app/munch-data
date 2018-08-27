package munch.data.catalyst;

import catalyst.airtable.AirtableApi;
import catalyst.edit.PlaceEdit;
import catalyst.edit.PlaceEditBuilder;
import catalyst.edit.StatusEdit;
import catalyst.elastic.ElasticQueryUtils;
import catalyst.link.PlaceLink;
import catalyst.mutation.PlaceMutation;
import catalyst.plugin.LinkPlugin;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterators;
import munch.data.location.Location;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 22/8/18
 * Time: 9:35 PM
 * Project: munch-data
 */
@Singleton
public final class RestrictedAreaPlugin extends LinkPlugin<RestrictedArea> {

    private final AirtableApi.Table table;

    @Inject
    public RestrictedAreaPlugin(AirtableApi airtableApi) {
        this.table = airtableApi.base("appERO4wuQ5oJSTxO").table("Restricted Area");
    }

    @Override
    public String getSource() {
        return "restricted.area.data.munch.space";
    }

    @Override
    protected Iterator<RestrictedArea> objects() {
        Iterator<RestrictedArea> iterator = Iterators.transform(table.select(), RestrictedArea::new);
        return Iterators.filter(iterator, area -> {
            if (StringUtils.isAnyBlank(area.getId(), area.getName())) return false;
            if (area.getLocation().getPolygon() != null) return true;

            if (!area.getLocationCondition().getPostcodes().isEmpty()) {
                Location location = area.getLocation();
                return location.getCity() != null;
            }
            return false;
        });
    }

    @Override
    protected String getId(RestrictedArea object) {
        return object.getId();
    }

    @Override
    protected Iterator<PlaceMutation> search(RestrictedArea area) {
        namedCounter.increment("RestrictedArea");

        List<String> points = getPoints(area);
        JsonNode bool = createBoolQuery(area.getLocationCondition().getPostcodes(), points);
        JsonNode query = JsonUtils.createObjectNode().set("bool", bool);
        return placeMutationClient.searchQuery(query);
    }

    private static JsonNode createBoolQuery(Collection<String> postcodes, List<String> points) {
        ObjectNode bool = JsonUtils.createObjectNode();
        bool.set("must", ElasticQueryUtils.matchAll());

        ArrayNode filter = bool.putArray("filter");
        filter.add(ElasticQueryUtils.filterPolygon("latLng.value", points));

        if (postcodes.isEmpty()) return bool;
        else if (postcodes.size() == 1) {
            filter.add(ElasticQueryUtils.filterTerm("postcode.value", postcodes.iterator().next()));
        } else {
            filter.add(ElasticQueryUtils.filterTerms("postcode.value", postcodes));
        }

        return bool;
    }

    private static List<String> getPoints(RestrictedArea area) {
        Location.@Valid Polygon polygon = area.getLocation().getPolygon();
        if (polygon != null) return polygon.getPoints();

        List<String> points = area.getLocation().getCity().getPoints();
        if (points != null) return points;

        throw new IllegalArgumentException("points not available");
    }

    @Nullable
    @Override
    protected PlaceEdit receive(RestrictedArea area, PlaceMutation placeMutation, @Nullable PlaceLink placeLink, @Nullable PlaceEdit placeEdit) {
        namedCounter.increment("Linked");
        return new PlaceEditBuilder(getSource(), area.getId())
                .withSort("0")
                .withName("Restricted Area")
                .withStatus(StatusEdit.Type.closedHidden)
                .build();
    }
}
