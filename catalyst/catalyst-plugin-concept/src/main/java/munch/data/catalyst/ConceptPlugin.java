package munch.data.catalyst;

import catalyst.airtable.AirtableApi;
import catalyst.edit.PlaceEdit;
import catalyst.edit.PlaceEditBuilder;
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

    @Inject
    public ConceptPlugin(AirtableApi airtableApi) {
        this.table = airtableApi.base("appERO4wuQ5oJSTxO").table("Concept");
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
        namedCounter.increment("Concept");

        List<String> points = concept.getLocation().getCountry().getPoints();
        Set<String> values = new HashSet<>();
        values.addAll(concept.getEquals());
        values.addAll(concept.getContains());
        values = values.stream().map(StringUtils::lowerCase).collect(Collectors.toSet());

        return placeMutationClient.searchBuilder()
                .withFilterPolygon("latLng.value", Objects.requireNonNull(points))
                .withMatchQueryString("name.value", ElasticQueryStringUtils.Operator.OR, values)
                .asIterator();
    }

    @Nullable
    @Override
    protected PlaceEdit receive(Concept concept, PlaceMutation placeMutation, @Nullable PlaceLink placeLink, @Nullable PlaceEdit placeEdit) {
        if (!validate(concept, placeMutation)) return null;

        namedCounter.increment("Linked");
        PlaceEditBuilder builder = new PlaceEditBuilder(getSource(), concept.getId())
                .withSort("0");

        for (String tag : concept.getTags()) {
            if (StringUtils.isBlank(tag)) continue;
            builder.withTag(tag.toLowerCase());
        }

        return builder.build();
    }

    private boolean validate(Concept name, PlaceMutation placeMutation) {
        for (String equal : name.getEquals()) {
            for (MutationField<String> field : placeMutation.getName()) {
                if (field.getValue().equalsIgnoreCase(equal)) return true;
            }
        }

        for (String contain : name.getContains()) {
            for (MutationField<String> field : placeMutation.getName()) {
                if (StringUtils.containsIgnoreCase(field.getValue(), contain)) return true;
            }
        }

        return false;
    }
}
