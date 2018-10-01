package munch.data.resolver.tag;

import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import munch.data.place.Place;
import munch.data.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
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

    private final TagMapper tagMapper;
    private final TagLevelResolver levelResolver;
    private final TagTimeResolver timeResolver;

    @Inject
    public TagResolver(TagMapper tagMapper, TagLevelResolver levelResolver, TagTimeResolver timeResolver) {

        this.tagMapper = tagMapper;
        this.levelResolver = levelResolver;
        this.timeResolver = timeResolver;
    }

    public List<Place.Tag> resolve(PlaceMutation mutation) {
        List<String> tags = reduce(mutation.getTag());

        List<Tag> accumulator = new ArrayList<>();
        // Level 1-4 Resolver, Look at Airtable
        accumulator.addAll(levelResolver.resolve(tags));

        // Timing tag Resolver
        accumulator.addAll(timeResolver.resolve(mutation));

        return tagMapper.mapDistinct(accumulator);
    }

    /**
     * @return reduced tags
     */
    private List<String> reduce(List<MutationField<String>> tags) {
        // Temporary method to reduce to remove v2 tags if other sources of tag exist
        List<String> nonV2Tags = tags.stream()
                .filter(this::hasNonV2)
                .map(MutationField::getValue)
                .collect(Collectors.toList());

        if (!nonV2Tags.isEmpty()) return nonV2Tags;

        // Return all
        return tags.stream()
                .map(MutationField::getValue)
                .collect(Collectors.toList());
    }

    /**
     * deprecate this fast
     */
    private boolean hasNonV2(MutationField<String> field) {
        for (MutationField.Source source : field.getSources()) {
            if (!source.getSource().equals("v2.catalyst.munch.space")) {
                return true;
            }
        }
        return false;
    }
}
