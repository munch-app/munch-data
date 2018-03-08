package munch.data.place.collector;

import com.google.common.collect.ImmutableSet;
import corpus.data.CatalystClient;
import corpus.data.CorpusClient;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import corpus.utils.FieldCollector;
import munch.data.place.group.PlaceTagGroup;
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

    protected final CorpusClient corpusClient;
    protected final CatalystClient catalystClient;

    protected final SynonymTagMapping synonymTagMapping;

    @Inject
    public TagCollector(CorpusClient corpusClient, CatalystClient catalystClient, SynonymTagMapping synonymTagMapping) {
        this.corpusClient = corpusClient;
        this.catalystClient = catalystClient;
        this.synonymTagMapping = synonymTagMapping;
    }

    public Group collect(String placeId) {
        return new Group(catalystClient.listCorpus(placeId));
    }

    public Group collect(List<CorpusData> list) {
        return new Group(list.iterator());
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

    public class Group extends FieldCollector {
        private final Set<PlaceTagGroup> groups;
        private final Set<String> all;
        private final Set<String> trusted;

        private Group(Iterator<CorpusData> iterator) {
            super(PlaceKey.tag);
            iterator.forEachRemaining(this::add);
            this.all = ImmutableSet.copyOf(collect().stream()
                    .map(s -> StringUtils.trimToNull(StringUtils.lowerCase(s)))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet())
            );
            this.trusted = ImmutableSet.copyOf(collect(
                    f -> CORPUS_NAME_TRUSTED.contains(f.getCorpusName()),
                    s -> StringUtils.trimToNull(StringUtils.lowerCase(s))
            ));
            this.groups = synonymTagMapping.resolveAll(all);
        }

        public Set<String> collectAny() {
            return all;
        }

        public Set<String> collectTrusted() {
            return trusted;
        }

        public List<String> collectExplicit() {
            List<String> collected = new ArrayList<>();
            collected.addAll(findTypes(groups, Set.of("Cuisine"), 1));
            collected.addAll(findTypes(groups, Set.of("Establishment"), 1));
            collected.addAll(findTypes(groups, Set.of("Amenities", "Occasion"), 2));
            return collected;
        }

        public List<String> collectExplicitIds() {
            return groups.stream()
                    .filter(groupTag -> Set.of("Cuisine", "Establishment", "Amenities", "Occasion")
                            .contains(groupTag.getType()))
                    // Sorted to highest order first
                    // Must be lowercase
                    .map(PlaceTagGroup::getRecordId)
                    // Must be ordered
                    .collect(Collectors.toList());
        }

        public List<String> collectImplicit() {
            return findTypes(groups, Set.of("Cuisine", "Establishment", "Amenities", "Occasion", "Food"), 1000);
        }

        public List<String> collectPredict() {
            return List.of();
        }
    }
}
