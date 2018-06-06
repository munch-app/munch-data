package munch.data;

import com.vividsolutions.jts.geom.Polygon;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by: Fuxing
 * Date: 5/6/18
 * Time: 11:46 AM
 * Project: munch-data
 */
class AreaBridgeTest {
    @Test
    void name() {
        Polygon polygon = AreaBridge.createPolygon("1.307336516516135,103.8568539666166", 0.3);
        List<String> points = AreaBridge.toPoints(polygon);
        System.out.println(AreaBridge.toWKT(points));
    }
}