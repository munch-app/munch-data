package munch.data.catalyst;

import catalyst.airtable.AirtableApi;
import catalyst.airtable.AirtableRecord;
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
import munch.data.Location;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.StreamSupport;

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
        this.table = airtableApi.base("appDcx5b3vgkhcYB5").table("Restricted Area");
    }

    @Override
    public String getSource() {
        return "restricted.area.data.munch.space";
    }

    @Override
    protected Iterator<RestrictedArea> objects() {
        Iterable<AirtableRecord> iterable = table::select;
        return StreamSupport.stream(iterable.spliterator(), false)
                .map(RestrictedArea::new)
                .filter(area -> {
                    if (StringUtils.isAnyBlank(area.getId(), area.getName())) return false;
                    if (area.getLocation().getPolygon() != null) return true;

                    if (!area.getLocationCondition().getPostcodes().isEmpty()) {
                        Location location = area.getLocation();
                        return StringUtils.isNotBlank(location.getCity());
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
        List<String> points = getPoints(area);

        ObjectNode root = JsonUtils.createObjectNode();
        root.put("from", 0);
        root.put("size", 300);

        JsonNode bool = createBoolQuery(area.getLocationCondition().getPostcodes(), points);
        root.set("query", JsonUtils.createObjectNode().set("bool", bool));

        JsonNode results = placeMutationClient.search(root);
        List<PlaceMutation> mutations = new ArrayList<>();
        for (JsonNode node : results.path("hits").path("hits")) {
            mutations.add(JsonUtils.toObject(node.path("_source"), PlaceMutation.class));
        }

        // Search on city and country
        return mutations.iterator();
    }

    private static JsonNode createBoolQuery(Collection<String> postcodes, List<String> points) {
        ObjectNode bool = JsonUtils.createObjectNode();
        bool.set("must", ElasticQueryUtils.matchAll());

        ArrayNode filter = bool.putArray("filter");

        filter.add(ElasticQueryUtils.filterPolygon("latLng.value", points));
        if (postcodes.isEmpty()) return bool;

        if (postcodes.size() == 1) {
            filter.add(ElasticQueryUtils.filterTerm("postcode.value", postcodes.iterator().next()));
        } else {
            filter.add(ElasticQueryUtils.filterTerms("postcode.value", postcodes));
        }

        return bool;
    }

    private static List<String> getPoints(RestrictedArea area) {
        Location.@Valid Polygon polygon = area.getLocation().getPolygon();
        if (polygon != null) return polygon.getPoints();

        switch (area.getLocation().getCity().toLowerCase()) {
            case "singapore":
                return List.of("1.26675774823251,103.60313415527344", "1.3244212231757635,103.61755371093749", "1.3896342476555246,103.65325927734375", "1.4143460858068593,103.66630554199219", "1.4294476354255539,103.67179870605467", "1.439057660807751,103.68278503417969", "1.4438626583311722,103.69583129882812", "1.4589640128389818,103.72055053710938", "1.4582775898253464,103.73771667480469", "1.4493540716333067,103.75419616699219", "1.4500404973607948,103.7603759765625", "1.4788701887242242,103.80363464355467", "1.4754381021049132,103.8269805908203", "1.4582775898253464,103.86680603027342", "1.4321933610794366,103.8922119140625", "1.4287612034988086,103.89701843261717", "1.4267019064882447,103.91555786132812", "1.4321933610794366,103.93478393554688", "1.4218968729661605,103.96018981933592", "1.4246426076343077,103.985595703125", "1.4212104387885494,104.00070190429688", "1.4397440896459617,104.02130126953125", "1.445921939876798,104.04396057128906", "1.4246426076343077,104.08721923828125", "1.3971851147344805,104.09477233886719", "1.3573711816421556,104.08103942871094", "1.290097884072079,104.12704467773438", "1.2777413679950957,104.12704467773438", "1.2537146393239096,103.98216247558594", "1.1754546449158993,103.81256103515625", "1.1301452152248344,103.73634338378906", "1.1905576261723045,103.65394592285156", "1.1960495988987414,103.56536865234375", "1.26675774823251,103.60313415527344");
        }
        throw new IllegalArgumentException("points not available");
    }

    @Nullable
    @Override
    protected PlaceEdit receive(RestrictedArea area, PlaceMutation placeMutation, @Nullable PlaceLink placeLink, @Nullable PlaceEdit placeEdit) {
        return new PlaceEditBuilder(getSource(), area.getId())
                .withStatus(StatusEdit.Type.closedHidden)
                .build();
    }
}
