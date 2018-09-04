package munch.data.catalyst;

import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import edit.utils.name.NameSimilarity;
import munch.data.brand.Brand;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        return match(brand, mutation.getName());
    }

    private boolean match(Brand brand, List<MutationField<String>> nameFields) {
        for (String brandName : getNames(brand)) {
            for (MutationField<String> nameField : nameFields) {
                if (nameSimilarity.equals(brandName, nameField.getValue())) {
                    return true;
                }
            }
        }

        return false;
    }

    private Set<String> getNames(Brand brand) {
        Set<String> names = new HashSet<>();
        names.add(brand.getName().toLowerCase());
        for (String name : brand.getNames()) {
            names.add(name.toLowerCase());
        }
        return names;
    }
}
