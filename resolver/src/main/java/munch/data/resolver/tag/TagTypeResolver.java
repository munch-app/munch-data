package munch.data.resolver.tag;

import catalyst.mutation.MutationField;
import catalyst.source.SourceMappingCache;
import munch.data.tag.Tag;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 1/10/18
 * Time: 3:38 PM
 * Project: munch-data
 */
@Singleton
public final class TagTypeResolver {
    protected static final Comparator<MutationField<Tag>> SORT_COMPARATOR;

    static {
        Comparator<MutationField<Tag>> comparator = Comparator.comparingLong(value ->
                value.getSources().size()
        );
        comparator = comparator.thenComparingLong(value ->
                value.getSources().stream()
                        .mapToLong(MutationField.Source::getMillis)
                        .max()
                        .orElse(0)
        );
        SORT_COMPARATOR = comparator.reversed();
    }

    private final SourceMappingCache mappingCache;

    @Inject
    public TagTypeResolver(SourceMappingCache mappingCache) {
        this.mappingCache = mappingCache;
    }

    public List<Tag> resolve(List<MutationField<Tag>> tags) {
        List<Tag> sorted = sorted(tags);
        return collect(sorted);
    }

    /**
     * @param tags to sort
     * @return non duplicated sorted in importance
     */
    private List<Tag> sorted(List<MutationField<Tag>> tags) {
        // Tag sorted by popularity, and then recency of the latest tag
        tags.sort(SORT_COMPARATOR);

        List<Tag> sorted = new ArrayList<>();

        // Collect 'Form' tags first
        tags.forEach(field -> {
            for (MutationField.Source source : field.getSources()) {
                if (mappingCache.isForm(source.getSource())) {
                    sorted.add(field.getValue());
                    return;
                }
            }
        });

        // Collect other tags
        tags.forEach(field -> {
            if (sorted.contains(field.getValue())) return;
            sorted.add(field.getValue());
        });

        return sorted;
    }

    /**
     * @param tags to collect
     * @return with various type limited to count via logic
     */
    private List<Tag> collect(List<Tag> tags) {
        List<Tag> collector = new ArrayList<>();
        collector.addAll(collect(Tag.Type.Cuisine, 2, tags));
        collector.addAll(collect(Tag.Type.Establishment, 2, tags));
        collector.addAll(collect(Tag.Type.Amenities, 2, tags));
        collector.addAll(collect(Tag.Type.Requirement, 100, tags));

        // Remaining: Up to 5
        tags.stream()
                .filter(tag -> {
                    if (tag.getType() == Tag.Type.Cuisine) return false;
                    if (tag.getType() == Tag.Type.Establishment) return false;
                    if (tag.getType() == Tag.Type.Amenities) return false;
                    if (tag.getType() == Tag.Type.Requirement) return false;
                    return true;
                })
                .limit(5)
                .forEach(collector::add);

        return collector;
    }

    private static List<Tag> collect(Tag.Type type, int limit, List<Tag> tags) {
        return tags.stream()
                .filter(tag -> tag.getType() == type)
                .limit(limit)
                .collect(Collectors.toList());
    }
}
