package munch.data;

import com.vividsolutions.jts.geom.Polygon;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Created by: Fuxing
 * Date: 5/6/18
 * Time: 11:46 AM
 * Project: munch-data
 */
class AreaBridgeTest {
    @Test
    void name() {
        Polygon polygon = SpatialUtils.createPolygon("1.307336516516135,103.8568539666166", 0.3);
        List<String> points = SpatialUtils.toPoints(polygon);
        System.out.println(SpatialUtils.toWKT(points));
    }
}