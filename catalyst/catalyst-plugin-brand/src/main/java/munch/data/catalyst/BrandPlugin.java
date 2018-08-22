package munch.data.catalyst;

import catalyst.edit.PlaceEdit;
import catalyst.elastic.ElasticQueryUtils;
import catalyst.link.PlaceLink;
import catalyst.mutation.PlaceMutation;
import catalyst.plugin.LinkPlugin;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import munch.data.brand.Brand;
import munch.data.client.BrandClient;
import munch.restful.core.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 15/8/18
 * Time: 11:36 PM
 * Project: munch-data
 */
@Singleton
public final class BrandPlugin extends LinkPlugin<Brand> {
    private static final Logger logger = LoggerFactory.getLogger(BrandPlugin.class);

    private final BrandClient brandClient;
    private final BrandComparator brandComparator;
    private final PlaceEditMapper brandEditParser;

    @Inject
    public BrandPlugin(BrandClient brandClient, BrandComparator brandComparator, PlaceEditMapper brandEditParser) {
        this.brandClient = brandClient;
        this.brandComparator = brandComparator;
        this.brandEditParser = brandEditParser;
    }

    @Override
    public String getSource() {
        return "brand.data.munch.space";
    }

    @Override
    protected Iterator<Brand> objects() {
        return brandClient.iterator();
    }

    @Override
    protected String getId(Brand object) {
        return object.getBrandId();
    }

    @Nullable
    @Override
    protected PlaceEdit receive(Brand brand, PlaceMutation placeMutation, @Nullable PlaceLink placeLink, @Nullable PlaceEdit placeEdit) {
        if (brandComparator.match(brand, placeMutation)) {
            return brandEditParser.parse(brand);
        }
        return null;
    }

    @Override
    public Iterator<PlaceMutation> search(Brand brand) {
        if (!brand.getPlace().isAutoLink()) return Collections.emptyIterator();

        List<String> names = getNames(brand);
        List<String> points = getPoints(brand);
        if (names.isEmpty() || points == null) {
            logger.warn("Names or Points is empty for Brand: {}", brand);
            return Collections.emptyIterator();
        }

        ObjectNode root = JsonUtils.createObjectNode();
        root.put("from", 0);
        root.put("size", 300);

        JsonNode bool = createBoolQuery(names, points);
        root.set("query", JsonUtils.createObjectNode().set("bool", bool));

        JsonNode results = placeMutationClient.search(root);
        List<PlaceMutation> mutations = new ArrayList<>();
        for (JsonNode node : results.path("hits").path("hits")) {
            mutations.add(JsonUtils.toObject(node.path("_source"), PlaceMutation.class));
        }

        // Search on city and country
        return mutations.iterator();
    }

    private static JsonNode createBoolQuery(List<String> names, List<String> points) {
        ObjectNode bool = JsonUtils.createObjectNode();

        // Match Country & City
        bool.set("filter", JsonUtils.createArrayNode()
                .add(ElasticQueryUtils.filterPolygon("latLng.value", points))
        );

        // Match Names
        if (names.isEmpty()) {
            bool.set("must", ElasticQueryUtils.matchAll());
        } else if (names.size() <= 1) {
            String name = names.get(0);
            bool.set("must", ElasticQueryUtils.match("name.value", name));
        } else {
            bool.put("minimum_should_match", 1);
            ArrayNode should = bool.putArray("should");
            for (String name : names) {
                should.add(ElasticQueryUtils.match("name.value", name));
            }
        }
        return bool;
    }

    private static List<String> getPoints(Brand brand) {
        if (brand.getLocation().getCountry() == null) return null;
        switch (brand.getLocation().getCountry().toLowerCase()) {
            case "sgp":
                return List.of("1.26675774823251,103.60313415527344", "1.3244212231757635,103.61755371093749", "1.3896342476555246,103.65325927734375", "1.4143460858068593,103.66630554199219", "1.4294476354255539,103.67179870605467", "1.439057660807751,103.68278503417969", "1.4438626583311722,103.69583129882812", "1.4589640128389818,103.72055053710938", "1.4582775898253464,103.73771667480469", "1.4493540716333067,103.75419616699219", "1.4500404973607948,103.7603759765625", "1.4788701887242242,103.80363464355467", "1.4754381021049132,103.8269805908203", "1.4582775898253464,103.86680603027342", "1.4321933610794366,103.8922119140625", "1.4287612034988086,103.89701843261717", "1.4267019064882447,103.91555786132812", "1.4321933610794366,103.93478393554688", "1.4218968729661605,103.96018981933592", "1.4246426076343077,103.985595703125", "1.4212104387885494,104.00070190429688", "1.4397440896459617,104.02130126953125", "1.445921939876798,104.04396057128906", "1.4246426076343077,104.08721923828125", "1.3971851147344805,104.09477233886719", "1.3573711816421556,104.08103942871094", "1.290097884072079,104.12704467773438", "1.2777413679950957,104.12704467773438", "1.2537146393239096,103.98216247558594", "1.1754546449158993,103.81256103515625", "1.1301452152248344,103.73634338378906", "1.1905576261723045,103.65394592285156", "1.1960495988987414,103.56536865234375", "1.26675774823251,103.60313415527344");
        }
        return null;
    }

    private static List<String> getNames(Brand brand) {
        List<String> names = new ArrayList<>();
        names.add(brand.getName());

        for (String name : brand.getNames()) {
            if (!names.contains(name)) {
                names.add(name);
            }
        }
        return names;
    }
}
