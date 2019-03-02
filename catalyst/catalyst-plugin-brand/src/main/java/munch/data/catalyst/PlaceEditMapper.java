package munch.data.catalyst;

import catalyst.edit.PlaceEdit;
import catalyst.edit.PlaceEditBuilder;
import catalyst.edit.PlaceEditBuilderFactory;
import catalyst.edit.StatusEdit;
import munch.data.brand.Brand;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Created by: Fuxing
 * Date: 16/8/18
 * Time: 7:03 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceEditMapper {
    private static final String SOURCE = "brand.data.munch.space";

    private final PlaceEditBuilderFactory builderFactory;

    @Inject
    public PlaceEditMapper(PlaceEditBuilderFactory builderFactory) {
        this.builderFactory = builderFactory;
    }

    @NotNull
    public PlaceEdit parse(Brand brand) {
        PlaceEditBuilder builder = builderFactory.create(SOURCE, brand.getBrandId());
        builder.withSort("0");
        builder.withCreatedMillis(0L); // < So that create millis won't be picked up
        builder.withUpdatedMillis(brand.getUpdatedMillis());

        builder.withPlatform(SOURCE, brand.getBrandId());
        brand.getImages().forEach(builder::withImage);

        builder.withWebsite(brand.getWebsite());
        builder.withDescription(brand.getDescription());
        builder.withPhone(brand.getPhone());

        if (brand.getMenu() != null) {
            builder.withMenuUrl(brand.getMenu().getUrl());
        }
        if (brand.getPrice() != null) {
            builder.withPricePerPax(brand.getPrice().getPerPax());
        }

        brand.getTags().forEach(tag -> builder.withTag(tag.getName()));

        builder.withName(brand.getName(), 100);
        brand.getNames().forEach(s -> builder.withName(s, 1));

        if (brand.getStatus().getType() == Brand.Status.Type.closed) {
            builder.withStatus(StatusEdit.Type.closed);
        }
        return builder.build();
    }
}
