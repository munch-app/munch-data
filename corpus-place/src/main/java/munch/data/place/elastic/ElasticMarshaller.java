package munch.data.place.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import corpus.data.CorpusData;
import munch.data.place.graph.matcher.Matcher;
import munch.data.place.graph.matcher.Searcher;
import munch.restful.core.JsonUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 31/3/2018
 * Time: 2:27 AM
 * Project: munch-data
 */
@Singleton
public final class ElasticMarshaller {
    private static final ObjectMapper objectMapper = JsonUtils.objectMapper;

    private final Set<String> requiredFields;
    private final Set<Searcher> searchers;

    @Inject
    public ElasticMarshaller(Set<Matcher> matchers, Set<Searcher> searchers) {
        this.searchers = searchers;

        this.requiredFields = matchers.stream()
                .flatMap(matcher -> matcher.requiredFields().stream())
                .collect(Collectors.toSet());
    }

    /**
     * @return fields required for matcher
     */
    public Set<String> getRequiredFields() {
        return requiredFields;
    }

    /**
     * @param field to normalize
     */
    public void normalizeFields(CorpusData.Field field) {
        for (Searcher searcher : searchers) {
            searcher.normalize(field);
        }
    }

    public List<CorpusData.Field> toFields(JsonNode fields) {
        List<CorpusData.Field> fieldList = new ArrayList<>();
        fields.fields().forEachRemaining(entry -> {
            String key = entry.getKey().replace('_', '.');
            if (entry.getValue().isArray()) {
                for (JsonNode node : entry.getValue()) {
                    fieldList.add(new CorpusData.Field(key, node.asText()));
                }
            } else {
                fieldList.add(new CorpusData.Field(key, entry.getValue().asText()));
            }
        });

        return fieldList;
    }

    public JsonNode toNodes(List<CorpusData.Field> fields) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        fields.stream()
                .filter(field -> getRequiredFields().contains(field.getKey()))
                .peek(this::normalizeFields)
                .collect(Collectors.toMap(CorpusData.Field::getKey, CorpusData.Field::getValue))
                .forEach((key, values) -> {
                    objectNode.set(key.replace('.', '_'), objectMapper.valueToTree(values));
                });

        return objectNode;
    }
}
