package munch.data.catalyst;

import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import edit.utils.name.NameSimilarity;
import munch.data.brand.Brand;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 19/8/18
 * Time: 7:12 PM
 * Project: munch-data
 */
@Singleton
public final class BrandComparator {

    private final NameSimilarity nameSimilarity;

    @Inject
    public BrandComparator(NameSimilarity nameSimilarity) {
        this.nameSimilarity = nameSimilarity;
    }

    public boolean match(Brand brand, PlaceMutation mutation) {
        for (String brandName : getBrandNames(brand)) {
            for (String mutationName : getMutationNames(mutation)) {
                if (nameSimilarity.equals(brandName, mutationName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Set<String> getMutationNames(PlaceMutation mutation) {
        Set<String> names = new HashSet<>();
        for (MutationField<String> field : mutation.getName()) {
            if (!hasOnlyBrandSource(field)) {
                names.add(field.getValue());
            }
        }
        return names;
    }

    private static boolean hasOnlyBrandSource(MutationField<?> field) {
        if (field.getSources().size() > 1) return false;
        return field.getSources().get(0).getSource().equals("brand.data.munch.space");
    }

    public static Set<String> getBrandNames(Brand brand) {
        Set<String> names = new HashSet<>();
        names.add(brand.getName());
        names.addAll(brand.getNames());

        return names.stream()
                .map(StringUtils::lowerCase)
                .map(StringUtils::normalizeSpace)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
