package munch.data.resolver;

import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;

import javax.inject.Singleton;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 12/8/18
 * Time: 2:11 PM
 * Project: munch-data
 */
@Singleton
public final class CreatedMillisResolver {

    public long resolve(PlaceMutation mutation) {
        List<MutationField<Long>> created = mutation.getCreated();
        if (created.isEmpty()) return mutation.getCreatedMillis();

        return created.stream()
                .mapToLong(MutationField::getValue)
                .min()
                .orElse(mutation.getCreatedMillis());
    }
}
