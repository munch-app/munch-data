package munch.data.resolver.name;

import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import catalyst.source.SourceMappingCache;
import catalyst.source.SourceType;
import edit.utils.name.NameBlocked;
import munch.data.resolver.ResolverHaltException;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by: Fuxing
 * Date: 1/10/18
 * Time: 3:50 PM
 * Project: munch-data
 */
@Singleton
public final class NameResolver {

    private final SourceMappingCache mappingCache;
    private final NameBlocked blocked;

    @Inject
    public NameResolver(SourceMappingCache mappingCache, NameBlocked blocked) {
        this.mappingCache = mappingCache;
        this.blocked = blocked;
    }

    /**
     * @return find single name
     */
    public String resolve(PlaceMutation mutation) {
        if (mutation.getName().isEmpty()) throw new ResolverHaltException("names");

        List<String> fields = stream(mutation).collect(Collectors.toList());
        if (fields.isEmpty()) {
            return StringUtils.trimToNull(mutation.getName().get(0).getValue());
        }
        return fields.get(0);
    }

    /**
     * @return all valid names
     */
    public Set<String> resolveAll(PlaceMutation mutation) {
        return stream(mutation)
                .collect(Collectors.toSet());
    }

    /**
     * @return stream of all valid and normalised
     */
    private Stream<String> stream(PlaceMutation mutation) {
        return mutation.getName().stream()
                .filter(this::valid)
                .map(MutationField::getValue)
                .map(StringUtils::trimToNull)
                .map(StringUtils::normalizeSpace)
                .filter(StringUtils::isNotBlank);
    }

    /**
     * @return whether field is valid
     */
    private boolean valid(MutationField<String> field) {
        for (MutationField.Source source : field.getSources()) {
            if (mappingCache.isTypeAny(source.getSource(), SourceType.form, SourceType.plugin)) {
                return true;
            }
        }

        return !blocked.isBlocked(field.getValue());
    }
}
