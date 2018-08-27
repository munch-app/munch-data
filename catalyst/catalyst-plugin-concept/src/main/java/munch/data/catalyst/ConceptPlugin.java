package munch.data.catalyst;

import catalyst.airtable.AirtableApi;
import catalyst.edit.PlaceEdit;
import catalyst.edit.PlaceEditBuilder;
import catalyst.elastic.ElasticQueryUtils;
import catalyst.link.PlaceLink;
import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import catalyst.plugin.LinkPlugin;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterators;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 27/8/2018
 * Time: 8:13 PM
 * Project: munch-data
 */
@Singleton
public final class ConceptPlugin extends LinkPlugin<Concept> {
    private static final Escaper QUERY_STRING_ESCAPE;

    static {
        // + - = && || > < ! ( ) { } [ ] ^ " ~ * ? : \ /
        QUERY_STRING_ESCAPE = Escapers.builder()
                .addEscape('+', "\\+")
                .addEscape('-', "\\-")
                .addEscape('&', "\\=")
                .addEscape('|', "\\|")
                .addEscape('<', "\\<")
                .addEscape('>', "\\>")
                .addEscape('!', "\\~")
                .addEscape('(', "\\(")
                .addEscape(')', "\\)")
                .addEscape('{', "\\{")
                .addEscape('}', "\\}")
                .addEscape('[', "\\[")
                .addEscape(']', "\\]")
                .addEscape('^', "\\^")
                .addEscape('\"', "\\\"")
                .addEscape('~', "\\~")
                .addEscape('*', "\\*")
                .addEscape('?', "\\?")
                .addEscape(':', "\\:")
                .addEscape('\\', "\\\\")
                .addEscape('/', "\\/")
                .build();
    }

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
        Iterator<Concept> iterator = Iterators.transform(table.select(), Concept::new);
        return Iterators.filter(iterator, name -> {
            if (name.getLocation().getCountry() == null) return false;
            if (StringUtils.isAnyBlank(name.getId(), name.getName())) return false;

            if (name.getEquals().isEmpty() && name.getContains().isEmpty()) return false;
            return false;
        });
    }

    @Override
    protected String getId(Concept concept) {
        return concept.getId();
    }

    @Override
    protected Iterator<PlaceMutation> search(Concept name) {
        namedCounter.increment("Concept");

        List<String> points = name.getLocation().getCountry().getPoints();
        JsonNode bool = createBoolQuery(name.getEquals(), name.getContains(), points);
        JsonNode query = JsonUtils.createObjectNode().set("bool", bool);
        return placeMutationClient.searchQuery(query);
    }

    @SuppressWarnings("Duplicates")
    private static JsonNode createBoolQuery(Collection<String> equals, Collection<String> contains, List<String> points) {
        ObjectNode bool = JsonUtils.createObjectNode();

        ArrayNode must = bool.putArray("must");
        must.add(queryString(equals, contains));

        ArrayNode filter = bool.putArray("filter");
        filter.add(ElasticQueryUtils.filterPolygon("latLng.value", points));
        return bool;
    }

    @SuppressWarnings("Duplicates")
    private static ObjectNode queryString(Collection<String> equals, Collection<String> contains) {
        List<String> strings = new ArrayList<>();
        for (String equal : equals) {
            strings.add(QUERY_STRING_ESCAPE.escape(equal));
        }

        for (String contain : contains) {
            strings.add(QUERY_STRING_ESCAPE.escape(contain));
        }

        String query = strings.stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(") OR (", "(", ")"));

        ObjectNode node = JsonUtils.createObjectNode();
        ObjectNode queryString = node.putObject("query_string");
        queryString.put("default_field", "name.value");
        queryString.put("query", query);
        return node;
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
