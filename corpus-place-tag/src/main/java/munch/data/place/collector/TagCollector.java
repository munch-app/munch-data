package munch.data.place.collector;

import com.google.common.collect.Lists;
import corpus.data.CatalystClient;
import corpus.data.CorpusClient;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import corpus.utils.FieldCollector;
import munch.data.place.group.PlaceTagGroup;
import munch.data.place.predict.PredictTagClient;
import munch.data.place.text.CollectedText;
import munch.data.place.text.TextCollector;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 5/3/2018
 * Time: 9:41 AM
 * Project: munch-data
 */
@Singleton
public final class TagCollector {
    private static final Logger logger = LoggerFactory.getLogger(TagCollector.class);
    private static final Set<String> CORPUS_NAME_TRUSTED = Set.of(
            "Sg.MunchSheet.PlaceInfo2", "Sg.MunchSheet.FranchisePlace", "Sg.Munch.PlaceAward"
    );
    private static final Set<String> CORPUS_NAME_BLOCKED = Set.of(
            "Sg.Munch.Place"
    );

    protected final CorpusClient corpusClient;
    protected final CatalystClient catalystClient;
    protected final TextCollector textCollector;

    protected final PredictTagClient predictTagClient;

    protected final SynonymTagMapping synonymTagMapping;

    @Inject
    public TagCollector(CorpusClient corpusClient, CatalystClient catalystClient, TextCollector textCollector, PredictTagClient predictTagClient, SynonymTagMapping synonymTagMapping) {
        this.corpusClient = corpusClient;
        this.catalystClient = catalystClient;
        this.textCollector = textCollector;
        this.predictTagClient = predictTagClient;
        this.synonymTagMapping = synonymTagMapping;
    }

    public TagBuilder collect(String placeId) {
        return new TagBuilder(placeId, Lists.newArrayList(catalystClient.listCorpus(placeId)));
    }

    public TagBuilder collect(String placeId, List<CorpusData> list) {
        return new TagBuilder(placeId, list);
    }

    /**
     * @param groupTags group of tags to search in
     * @param types     type to filter out
     * @param limit     limit per type group
     * @return lowercase groups of tags
     */
    private List<String> findTypes(Set<PlaceTagGroup> groupTags, Set<String> types, int limit) {
        return groupTags.stream()
                .filter(group -> types.contains(group.getType()))
                // Sorted to highest order first
                .sorted((o1, o2) -> Double.compare(o2.getOrder(), o1.getOrder()))
                .limit(limit)
                // Must be lowercase
                .map(groupTag -> groupTag.getName().toLowerCase())
                // Must be ordered so a list
                .collect(Collectors.toList());
    }

    public class TagBuilder {
        private final String placeId;
        private final List<CorpusData> dataList;

        private final Set<String> tags = new HashSet<>();

        private TagBuilder(String placeId, List<CorpusData> list) {
            this.placeId = placeId;
            this.dataList = list;
            list.removeIf(data -> CORPUS_NAME_BLOCKED.contains(data.getCorpusName()));
        }

        public List<String> withAll() {
            FieldCollector fieldCollector = new FieldCollector(PlaceKey.tag);
            fieldCollector.addAll(dataList);
            List<String> collected = fieldCollector.collect().stream()
                    .map(s -> StringUtils.trimToNull(StringUtils.lowerCase(s)))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            tags.addAll(collected);
            return collected;
        }

        public List<String> withTrusted() {
            FieldCollector fieldCollector = new FieldCollector(PlaceKey.tag);
            fieldCollector.addAll(dataList);
            List<String> collected = fieldCollector.collect(
                    f -> CORPUS_NAME_TRUSTED.contains(f.getCorpusName()),
                    s -> StringUtils.trimToNull(StringUtils.lowerCase(s)));

            tags.addAll(collected);
            return collected;
        }

        public List<String> withPredicted() {
            List<CollectedText> collectedTexts = textCollector.collect(placeId, dataList);
            if (collectedTexts.isEmpty()) return List.of();

            List<String> texts = collectedTexts.stream()
                    .flatMap(collectedText -> collectedText.getTexts().stream())
                    .collect(Collectors.toList());

            Map<String, Double> labels = predictTagClient.predict(texts);
            if (labels.isEmpty()) return List.of();

            List<String> collected = new ArrayList<>();
            labels.forEach((k, value) -> {
                if (synonymTagMapping.isPredictable(k, value)) {
                    String tag = k.toLowerCase();
                    tags.add(tag);
                    collected.add(tag);
                }
            });

            return collected;
        }

        public List<String> collectString() {
            return new ArrayList<>(tags);
        }

        public Set<PlaceTagGroup> collectGroups() {
            return synonymTagMapping.resolveAll(tags);
        }

        public List<String> collectExplicit() {
            Set<PlaceTagGroup> groups = synonymTagMapping.resolveAll(tags);

            List<String> collected = new ArrayList<>();
            collected.addAll(findTypes(groups, Set.of("Cuisine"), 1));
            collected.addAll(findTypes(groups, Set.of("Establishment"), 1));
            collected.addAll(findTypes(groups, Set.of("Amenities", "Occasion"), 1));
            return collected;
        }

        public List<String> collectImplicit() {
            Set<PlaceTagGroup> groups = synonymTagMapping.resolveAll(tags);

            return findTypes(groups, Set.of("Cuisine", "Establishment", "Amenities", "Occasion", "Food"), 1000);
        }
    }
}
