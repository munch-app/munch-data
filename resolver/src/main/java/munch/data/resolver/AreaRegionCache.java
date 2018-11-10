package munch.data.resolver;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import munch.data.client.AreaClient;
import munch.data.location.Area;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 4/9/18
 * Time: 7:24 PM
 * Project: munch-data
 */
@Singleton
public final class AreaRegionCache {
    private final GeometryFactory geometryFactory = new GeometryFactory();

    private final List<NamedPolygon> polygons = new ArrayList<>();

    @Inject
    public AreaRegionCache(AreaClient areaClient) {
        areaClient.iterator().forEachRemaining(area -> {
            if (area.getType() != Area.Type.Region) return;
            if (area.getLocation().getPolygon() == null) return;
            Coordinate[] coordinates = area.getLocation().getPolygon().getPoints().stream()
                    .map(s -> s.split(","))
                    .map(ll -> new Coordinate(Double.parseDouble(ll[1]), Double.parseDouble(ll[0])))
                    .toArray(Coordinate[]::new);

            Polygon polygon = geometryFactory.createPolygon(coordinates);
            this.polygons.add(new NamedPolygon(area.getName(), polygon));
        });
    }

    @Nullable
    public String getName(double lat, double lng) {
        Point point = geometryFactory.createPoint(new Coordinate(lng, lat));
        return polygons.stream()
                .filter(p -> p.polygon.intersects(point))
                .min(Comparator.comparingDouble(value -> value.polygon.getArea()))
                .map(namedPolygon -> namedPolygon.name)
                .orElse(null);
    }

    private final class NamedPolygon {
        private final String name;
        private final Polygon polygon;

        public NamedPolygon(String name, Polygon polygon) {
            this.name = name;
            this.polygon = polygon;
        }
    }
}
