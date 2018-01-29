package munch.data.container;

import catalyst.utils.LatLngUtils;
import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import corpus.data.CorpusData;
import corpus.field.ContainerKey;
import munch.data.structure.Container;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 29/1/2018
 * Time: 6:27 PM
 * Project: munch-data
 */
public final class PolygonMatcher {
    private static final WKTReader reader = new WKTReader();
    private static final GeometryFactory factory = new GeometryFactory();

    private final List<Matched> matchedList = new ArrayList<>();

    public void put(CorpusData sourceData, Container container) {
        String matching = ContainerKey.matching.getValue(sourceData);
        if (!StringUtils.equals(matching, "polygon")) return;

        ContainerKey.Location.polygon.getAll(sourceData).forEach(field -> {
            Polygon polygon = read(field.getValue());
            matchedList.add(new Matched(polygon, sourceData, container));
        });
    }

    public List<CorpusData> find(LatLngUtils.LatLng latLng, String catalystId, long cycleNo) {
        Coordinate coordinate = new Coordinate(latLng.getLng(), latLng.getLat());
        Point point = factory.createPoint(coordinate);

        return matchedList.stream()
                .filter(matched -> matched.polygon.intersects(point))
                .map(matched -> matched.createPlace(catalystId, cycleNo))
                .collect(Collectors.toList());
    }

    private static Polygon read(String value) {
        try {
            return (Polygon) reader.read(value);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private class Matched {
        private final Polygon polygon;
        private final Container container;
        private final ImmutableList<CorpusData.Field> fields;
        private long matchedPlaces = 0;

        public Matched(Polygon polygon, CorpusData data, Container container) {
            this.polygon = polygon;
            this.container = container;
            this.fields = ImmutableList.copyOf(data.getFields());
        }


        /**
         * @param cycleNo cycleNo
         * @return newly created Sg.MunchSheet.FranchisePlace
         */
        @SuppressWarnings("Duplicates")
        public CorpusData createPlace(String catalystId, long cycleNo) {
            matchedPlaces++;

            CorpusData data = new CorpusData("Sg.Munch.ContainerPlace", catalystId, cycleNo);
            data.setCatalystId(catalystId);
            data.setFields(fields);
            return data;
        }

        /**
         * @return null if container contains too little data
         */
        @Nullable
        public Container getContainer() {
            container.setCount(matchedPlaces);
            return container;
        }
    }

    public void forEach(Consumer<Container> consumer) {
        matchedList.forEach(matched -> {
            consumer.accept(matched.getContainer());
        });
    }
}
