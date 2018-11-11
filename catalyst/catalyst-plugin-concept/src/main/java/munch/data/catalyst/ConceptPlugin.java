package munch.data.catalyst;

import catalyst.airtable.AirtableApi;
import catalyst.edit.PlaceEdit;
import catalyst.edit.PlaceEditBuilderFactory;
import catalyst.elastic.ElasticQueryStringUtils;
import catalyst.link.PlaceLink;
import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import catalyst.plugin.LinkPlugin;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 27/8/2018
 * Time: 8:13 PM
 * Project: munch-data
 */
@Singleton
public final class ConceptPlugin extends LinkPlugin<Concept> {

    private final AirtableApi.Table table;
    private final PlaceEditBuilderFactory builderFactory;

    @Inject
    public ConceptPlugin(AirtableApi airtableApi, PlaceEditBuilderFactory builderFactory) {
        this.table = airtableApi.base("appERO4wuQ5oJSTxO").table("Concept");
        this.builderFactory = builderFactory;
    }

    @Override
    public String getSource() {
        return "concept.data.munch.space";
    }

    @Override
    protected Iterator<Concept> objects() {
        return Lists.newArrayList(table.select()).stream()
                .map(Concept::new)
                .filter(concept -> {
                    if (concept.getLocation().getCountry() == null) return false;
                    if (StringUtils.isAnyBlank(concept.getId(), concept.getName())) return false;
                    if (concept.getTags().isEmpty()) return false;
                    if (concept.getEquals().isEmpty() && concept.getContains().isEmpty()) return false;

                    return true;
                }).iterator();
    }

    @Override
    protected String getId(Concept concept) {
        return concept.getId();
    }

    @Override
    protected Iterator<PlaceMutation> search(Concept concept) {
        counter.increment("Concept");

        List<String> points = concept.getLocation().getCountry().getPoints();
        Set<String> values = new HashSet<>();
        values.addAll(concept.getEquals());
        values.addAll(concept.getContains());
        values = values.stream().map(StringUtils::lowerCase).collect(Collectors.toSet());

        return placeMutationClient.searchBuilder()
                .withFilterPolygon("latLng.value", Objects.requireNonNull(points))
                .withMustQueryString("name.value", ElasticQueryStringUtils.Operator.OR, values)
                .asIterator();
    }

    @Nullable
    @Override
    protected PlaceEdit receive(Concept concept, PlaceMutation placeMutation, @Nullable PlaceLink placeLink, @Nullable PlaceEdit placeEdit) {
        if (!isValid(concept, placeMutation)) return null;

        counter.increment("Linked");
        return builderFactory.create(getSource(), concept.getId())
                .withTags(concept.getTags())
                .withSort("0")
                .build();
    }

    private boolean isValid(Concept concept, PlaceMutation placeMutation) {
        if (placeMutation == null) return false;

        for (String equal : concept.getEquals()) {
            for (MutationField<String> field : placeMutation.getName()) {
                if (field.getValue().equalsIgnoreCase(equal)) return true;
            }
        }

        for (String contain : concept.getContains()) {
            for (MutationField<String> field : placeMutation.getName()) {
                if (StringUtils.containsIgnoreCase(field.getValue(), contain)) return true;
            }
        }

        return false;
    }
}
