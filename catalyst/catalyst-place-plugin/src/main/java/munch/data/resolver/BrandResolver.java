package munch.data.resolver;

import catalyst.edit.PlatformEdit;
import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceMutation;
import munch.data.brand.Brand;
import munch.data.client.BrandClient;
import munch.data.place.Place;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 8/8/18
 * Time: 4:35 PM
 * Project: munch-data
 */
@Singleton
public final class BrandResolver {

    private final BrandClient brandClient;

    @Inject
    public BrandResolver(BrandClient brandClient) {
        this.brandClient = brandClient;
    }

    public Place.Brand resolve(PlaceMutation mutation) {
        String brandId = findBrandId(mutation);
        if (brandId != null) {
            return resolve(brandId);
        }
        return null;
    }

    private Place.Brand resolve(String brandId) {
        Brand brand = brandClient.get(brandId);
        if (brand == null) return null;

        Place.Brand placeBrand = new Place.Brand();
        placeBrand.setBrandId(brandId);
        placeBrand.setName(brand.getName());
        return placeBrand;
    }

    private String findBrandId(PlaceMutation mutation) {
        for (MutationField<PlatformEdit> field : mutation.getPlatform()) {
            PlatformEdit platform = field.getValue();
            if (platform.getName().equals("brand.data.munch.space")) {
                return platform.getId();
            }
        }
        return null;
    }
}
