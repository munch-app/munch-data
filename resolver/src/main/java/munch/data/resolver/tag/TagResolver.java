package munch.data.resolver.tag;

import catalyst.license.LicenseValueSanitizer;
import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import munch.data.client.TagClient;
import munch.data.place.Place;
import munch.data.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 6/6/18
 * Time: 6:15 PM
 * Project: munch-data
 */
@Singleton
public final class TagResolver {
    private static final Logger logger = LoggerFactory.getLogger(TagResolver.class);

    private final Supplier<TagMapper> tagMapper;
    private final TagTypeResolver typeResolver;
    private final TagTimeResolver timeResolver;

    private final LicenseValueSanitizer sanitizer;

    @Inject
    public TagResolver(TagClient tagClient, TagTypeResolver typeResolver, TagTimeResolver timeResolver, LicenseValueSanitizer sanitizer) {
        this.tagMapper = Suppliers.memoizeWithExpiration(() -> new TagMapper(tagClient), 1, TimeUnit.DAYS);
        this.typeResolver = typeResolver;
        this.timeResolver = timeResolver;
        this.sanitizer = sanitizer;
    }

    public List<Place.Tag> resolve(PlaceMutation mutation) {
        List<Tag> accumulator = new ArrayList<>();

        // Level Type Resolver
        List<MutationField<Tag>> tags = reduce(mutation);
        accumulator.addAll(typeResolver.resolve(tags));

        // Timing tag Resolver
        accumulator.addAll(timeResolver.resolve(mutation));

        return tagMapper.get().mapDistinct(accumulator);
    }

    /**
     * Reduces into Set of Tag with licensing validated
     *
     * @param mutation to reduce into tags
     * @return List of Mutation Tag sanitized without duplication
     */
    private List<MutationField<Tag>> reduce(PlaceMutation mutation) {
        List<MutationField<Tag>> fields = reduceFields(mutation);
        sanitizer.sanitize(fields);

        return fields;
    }

    /**
     * Reduce Tag into a List with it's sources
     * Reduced tag will join similar tag and combine it's sources
     */
    private List<MutationField<Tag>> reduceFields(PlaceMutation mutation) {
        Map<Tag, Set<MutationField.Source>> tagSources = new HashMap<>();
        TagMapper mapper = this.tagMapper.get();

        // Collect Tag into Tag and it's Sources
        mutation.getTag().forEach(field -> mapper.get(field.getValue()).forEach(tag -> {
            Set<MutationField.Source> sources = tagSources.computeIfAbsent(tag, t -> new HashSet<>());
            sources.addAll(field.getSources());
        }));

        // Reduce into List of MutationField with Tag as its Value
        return tagSources.entrySet().stream()
                .map(TagResolver::toField)
                .collect(Collectors.toList());
    }


    /**
     * @param entry to convert
     * @return to MutationField with Tag to use License Value Sanitizer
     */
    private static MutationField<Tag> toField(Map.Entry<Tag, Set<MutationField.Source>> entry) {
        MutationField<Tag> field = new MutationField<>();
        field.setValue(entry.getKey());
        field.setSources(new ArrayList<>(entry.getValue()));
        return field;
    }
}
