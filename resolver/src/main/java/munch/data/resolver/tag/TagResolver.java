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
import javax.validation.constraints.NotNull;
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
    private final TagLevelResolver levelResolver;
    private final TagTimeResolver timeResolver;

    private final LicenseValueSanitizer sanitizer;

    @Inject
    public TagResolver(TagClient tagClient, TagLevelResolver levelResolver, TagTimeResolver timeResolver, LicenseValueSanitizer sanitizer) {
        this.tagMapper = Suppliers.memoizeWithExpiration(() -> new TagMapper(tagClient), 1, TimeUnit.DAYS);
        this.levelResolver = levelResolver;
        this.timeResolver = timeResolver;
        this.sanitizer = sanitizer;
    }

    public List<Place.Tag> resolve(PlaceMutation mutation) {
        Set<@NotNull Tag> tags = reduce(mutation.getTag());

        List<Tag> accumulator = new ArrayList<>();
        // Level 1-4 Resolver, Look at Airtable
        accumulator.addAll(levelResolver.resolve(tags));

        // Timing tag Resolver
        accumulator.addAll(timeResolver.resolve(mutation));

        return tagMapper.get().mapDistinct(accumulator);
    }

    private Set<@NotNull Tag> reduce(List<MutationField<String>> tags) {
        Map<Tag, Set<MutationField.Source>> tagSources = new HashMap<>();

        tags.forEach(field -> tagMapper.get().get(field.getValue()).forEach(tag -> {
            Set<MutationField.Source> sources = tagSources.computeIfAbsent(tag, t -> new HashSet<>());
            sources.addAll(field.getSources());
        }));

        List<MutationField<Tag>> fields = tagSources.entrySet().stream().map(this::toField).collect(Collectors.toList());

        sanitizer.sanitize(fields);

        return fields.stream()
                .map(MutationField::getValue)
                .collect(Collectors.toSet());
    }

    private MutationField<Tag> toField(Map.Entry<Tag, Set<MutationField.Source>> entry) {
        MutationField<Tag> field = new MutationField<>();
        field.setValue(entry.getKey());
        field.setSources(new ArrayList<>(entry.getValue()));
        return field;
    }
}
