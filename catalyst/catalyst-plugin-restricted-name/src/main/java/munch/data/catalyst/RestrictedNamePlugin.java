package munch.data.catalyst;

import catalyst.airtable.AirtableApi;
import catalyst.edit.PlaceEdit;
import catalyst.edit.PlaceEditBuilderFactory;
import catalyst.edit.StatusEdit;
import catalyst.elastic.ElasticQueryStringUtils;
import catalyst.link.PlaceLink;
import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import catalyst.plugin.LinkPlugin;
import catalyst.source.SourceMappingCache;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 22/8/18
 * Time: 9:35 PM
 * Project: munch-data
 */
@Singleton
public final class RestrictedNamePlugin extends LinkPlugin<RestrictedName> {

    private final SourceMappingCache mappingCache;
    private final AirtableApi.Table table;

    private final PlaceEditBuilderFactory builderFactory;

    @Inject
    public RestrictedNamePlugin(SourceMappingCache mappingCache, AirtableApi airtableApi, PlaceEditBuilderFactory builderFactory) {
        this.mappingCache = mappingCache;
        this.table = airtableApi.base("appERO4wuQ5oJSTxO").table("Restricted Name");
        this.builderFactory = builderFactory;
    }

    @Override
    public String getSource() {
        return "restricted.name.data.munch.space";
    }

    @Override
    protected Iterator<RestrictedName> objects() {
        return Lists.newArrayList(table.select()).stream()
                .map(RestrictedName::new)
                .filter(name -> {
                    if (StringUtils.isAnyBlank(name.getId(), name.getName())) return false;
                    if (name.getLocation().getCountry() == null) return false;

                    if (name.getEquals().isEmpty() && name.getContains().isEmpty()) return false;
                    return true;
                }).iterator();
    }

    @Override
    protected String getId(RestrictedName object) {
        return object.getId();
    }

    @Override
    protected Iterator<PlaceMutation> search(RestrictedName name) {
        namedCounter.increment("Restricted Name");

        List<String> points = name.getLocation().getCountry().getPoints();
        Set<String> values = new HashSet<>();
        values.addAll(name.getEquals());
        values.addAll(name.getContains());
        values = values.stream().map(StringUtils::lowerCase).collect(Collectors.toSet());

        return placeMutationClient.searchBuilder()
                .withFilterPolygon("latLng.value", Objects.requireNonNull(points))
                .withMatchQueryString("name.value", ElasticQueryStringUtils.Operator.OR, values)
                .asIterator();
    }

    @Nullable
    @Override
    protected PlaceEdit receive(RestrictedName name, PlaceMutation placeMutation, @Nullable PlaceLink placeLink, @Nullable PlaceEdit placeEdit) {
        if (!validate(name, placeMutation)) return null;

        namedCounter.increment("Linked");
        return builderFactory.create(getSource(), name.getId())
                .withSort("0")
                .withStatus(StatusEdit.Type.closedHidden)
                .build();
    }

    private boolean validate(RestrictedName name, PlaceMutation placeMutation) {
        for (MutationField<String> nameField : placeMutation.getName()) {
            for (MutationField.Source source : nameField.getSources()) {
                if (mappingCache.isForm(source.getSource())) return false;
            }
        }

        for (String equal : name.getEquals()) {
            for (MutationField<String> field : placeMutation.getName()) {
                if (field.getValue().equalsIgnoreCase(equal)) return true;
            }
        }

        for (String contain : name.getContains()) {
            contain = Pattern.quote(contain);
            Pattern pattern = Pattern.compile("(^|\\s|[^a-z0-9])" + contain + "($|\\s|[^a-z0-9])", Pattern.CASE_INSENSITIVE);
            for (MutationField<String> field : placeMutation.getName()) {
                if (pattern.matcher(field.getValue()).find()) return true;
            }
        }

        return false;
    }
}
