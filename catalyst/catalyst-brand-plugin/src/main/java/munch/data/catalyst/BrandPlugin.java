package munch.data.catalyst;

import catalyst.edit.PlaceEdit;
import catalyst.elastic.ElasticQueryUtils;
import catalyst.link.PlaceLink;
import catalyst.mutation.PlaceMutation;
import catalyst.plugin.EditPlugin;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterators;
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
public final class BrandPlugin extends EditPlugin {
    private static final Logger logger = LoggerFactory.getLogger(BrandPlugin.class);

    private final BrandClient brandClient;

    @Inject
    public BrandPlugin(BrandClient brandClient) {
        this.brandClient = brandClient;
    }

    @Override
    public String getSource() {
        return "brand.data.munch.space";
    }

    @Nullable
    @Override
    protected PlaceEdit receive(PlaceMutation placeMutation, @Nullable PlaceLink placeLink, @Nullable PlaceEdit placeEdit) {
        // TODO PD-247

        // TODO Pre linked
        return null;
    }

    @Override
    protected Iterator<PlaceMutation> collectMutations() {
        Iterator<Brand> brandIterator = brandClient.iterator();
        Iterator<Iterator<PlaceMutation>> mutations = Iterators.transform(brandIterator, this::search);
        return Iterators.concat(mutations);
    }

    private Iterator<PlaceMutation> search(Brand brand) {
        // TODO search
        return Collections.emptyIterator();
    }

    private List<PlaceMutation> search(List<String> names, String latLng, int from, int size) {
        ObjectNode root = JsonUtils.createObjectNode();
        root.put("from", from);
        root.put("size", size);

        JsonNode bool = createBoolQuery(names, latLng);
        root.set("query", JsonUtils.createObjectNode().set("bool", bool));

        JsonNode results = placeMutationClient.search(root);
        List<PlaceMutation> mutations = new ArrayList<>();
        for (JsonNode node : results.path("hits").path("hits")) {
            mutations.add(JsonUtils.toObject(node.path("_source"), PlaceMutation.class));
        }
        return mutations;
    }

    private static JsonNode createBoolQuery(List<String> names, String latLng) {
        ObjectNode bool = JsonUtils.createObjectNode();
        bool.set("filter", JsonUtils.createArrayNode()
                .add(ElasticQueryUtils.filterDistance(
                        "latLng.value", latLng, 150))
        );

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
}
