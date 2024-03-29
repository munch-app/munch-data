package munch.data.catalyst;

import catalyst.airtable.AirtableApi;
import catalyst.edit.PlaceEdit;
import catalyst.edit.PlaceEditBuilderFactory;
import catalyst.edit.StatusEdit;
import catalyst.elastic.ElasticSearchBuilder;
import catalyst.link.PlaceLink;
import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import catalyst.plugin.LinkPlugin;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import munch.data.location.Area;
import munch.data.location.Location;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 22/8/18
 * Time: 9:35 PM
 * Project: munch-data
 */
@Singleton
public final class RestrictedAreaPlugin extends LinkPlugin<RestrictedArea> {
    private static GeometryFactory geometryFactory = new GeometryFactory();

    private final AirtableApi.Table table;
    private final PlaceEditBuilderFactory builderFactory;

    @Inject
    public RestrictedAreaPlugin(AirtableApi airtableApi, PlaceEditBuilderFactory builderFactory) {
        this.table = airtableApi.base("appERO4wuQ5oJSTxO").table("Restricted Area");
        this.builderFactory = builderFactory;
    }

    @Override
    public String getSource() {
        return "restricted.area.data.munch.space";
    }

    @Override
    protected Iterator<RestrictedArea> objects() {
        return Lists.newArrayList(table.select()).stream()
                .map(RestrictedArea::new)
                .filter(area -> {
                    if (StringUtils.isAnyBlank(area.getId(), area.getName())) return false;
                    if (area.getLocation().getPolygon() != null) return true;

                    if (!area.getLocationCondition().getPostcodes().isEmpty()) {
                        Location location = area.getLocation();
                        return location.getCity() != null;
                    }
                    return false;
                }).iterator();
    }

    @Override
    protected String getId(RestrictedArea object) {
        return object.getId();
    }

    @Override
    protected Iterator<PlaceMutation> search(RestrictedArea area) {
        counter.increment("Restricted Area");

        List<String> points = getPoints(area);
        Set<String> postcodes = area.getLocationCondition().getPostcodes();

        ElasticSearchBuilder<PlaceMutation> searchBuilder = placeMutationClient.searchBuilder();
        searchBuilder.withFilterPolygon("latLng.value", points);

        if (postcodes.size() == 1) {
            searchBuilder.withFilterTerm("postcode.value", postcodes.iterator().next());
        } else if (postcodes.size() > 1) {
            searchBuilder.withFilterTerms("postcode.value", postcodes);
        }

        return searchBuilder.asIterator();
    }

    private static List<String> getPoints(RestrictedArea area) {
        Location.@Valid Polygon polygon = area.getLocation().getPolygon();
        if (polygon != null) return polygon.getPoints();

        List<String> points = area.getLocation().getCountry().getPoints();
        if (points != null) return points;

        throw new IllegalArgumentException("points not available");
    }

    @Nullable
    @Override
    protected PlaceEdit receive(RestrictedArea area, PlaceMutation placeMutation, @Nullable PlaceLink placeLink, @Nullable PlaceEdit placeEdit) {
        if (!validate(area, placeMutation)) return null;

        counter.increment("Linked");
        return builderFactory.create(getSource(), area.getId())
                .withSort("0")
                .withStatus(StatusEdit.Type.closedHidden)
                .build();
    }

    private boolean validate(RestrictedArea area, PlaceMutation placeMutation) {
        if (!validateLocation(area, placeMutation)) return false;
        Area.LocationCondition condition = area.getLocationCondition();
        if (condition.getPostcodes().isEmpty()) return true;

        for (String postcode : condition.getPostcodes()) {
            for (MutationField<String> field : placeMutation.getPostcode()) {
                if (postcode.equalsIgnoreCase(field.getValue())) return true;
            }
        }

        return false;
    }

    private boolean validateLocation(RestrictedArea area, PlaceMutation placeMutation) {
        Polygon polygon = toPolygon(getPoints(area));
        for (MutationField<String> field : placeMutation.getLatLng()) {
            String[] latLng = field.getValue().split(",");
            Coordinate coordinate = new Coordinate(
                    Double.parseDouble(latLng[1]),
                    Double.parseDouble(latLng[0])
            );

            Point point = geometryFactory.createPoint(coordinate);
            if (polygon.intersects(point)) return true;
        }
        return false;
    }

    private Polygon toPolygon(List<String> points) {
        return geometryFactory.createPolygon(points.stream()
                .map(s -> {
                    String[] split = s.split(",");
                    return new Coordinate(
                            Double.parseDouble(split[1]),
                            Double.parseDouble(split[0])
                    );
                }).toArray(Coordinate[]::new));
    }
}
