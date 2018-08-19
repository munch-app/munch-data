package munch.data.catalyst;

import catalyst.edit.*;
import munch.data.brand.Brand;
import munch.file.Image;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 16/8/18
 * Time: 7:03 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceEditMapper {

    public PlaceEdit parse(Brand brand) {
        PlaceEdit root = new PlaceEdit();
        root.setId(brand.getBrandId());
        root.setSource("brand.data.munch.space");
        root.setSort("0");
        root.setCreatedMillis(0L); // < So that create millis won't be picked up
        root.setUpdatedMillis(brand.getUpdatedMillis());

        PlaceEdit.Edit edit = new PlaceEdit.Edit();
        edit.setNames(mapNames(brand));
        edit.setImages(mapImages(brand.getImages()));
        edit.setTags(mapTags(brand));
        edit.setStatus(mapStatus(brand));
        edit.setContact(mapContact(brand));
        edit.setMenu(mapMenu(brand));
        edit.setAbout(mapAbout(brand));
        edit.setPlatforms(mapPlatforms(brand));
        root.setEdit(edit);
        return root;
    }

    private List<PlatformEdit> mapPlatforms(Brand brand) {
        PlatformEdit edit = new PlatformEdit();
        edit.setName("brand.data.munch.space");
        edit.setId(brand.getBrandId());
        return List.of(edit);
    }

    private List<NameEdit> mapNames(Brand brand) {
        List<NameEdit> nameEdits = new ArrayList<>();

        NameEdit nameEdit = new NameEdit();
        nameEdit.setName(brand.getName());
        nameEdit.setOrder(100);
        nameEdits.add(nameEdit);

        for (String name : brand.getNames()) {
            NameEdit edit = new NameEdit();
            edit.setName(name);
            edit.setOrder(1);
            nameEdits.add(edit);
        }

        return nameEdits;
    }

    @Nullable
    private List<ImageEdit> mapImages(List<Image> images) {
        if (images.isEmpty()) return null;

        return images.stream()
                .map(image -> {
                    ImageEdit edit = new ImageEdit();
                    edit.setSizes(image.getSizes());
                    edit.setImageId(image.getImageId());
                    return edit;
                })
                .collect(Collectors.toList());
    }

    @Nullable
    private ContactEdit mapContact(Brand brand) {
        if (StringUtils.isAnyBlank(brand.getPhone())) return null;

        ContactEdit edit = new ContactEdit();
        edit.setPhone(brand.getPhone());
        return edit;
    }

    @Nullable
    private List<TagEdit> mapTags(Brand brand) {
        if (brand.getTags().isEmpty()) return null;

        List<TagEdit> list = new ArrayList<>();
        for (Brand.Tag tag : brand.getTags()) {
            TagEdit edit = new TagEdit();
            edit.setName(tag.getName().toLowerCase());
            list.add(edit);
        }
        return list;
    }

    private StatusEdit mapStatus(Brand brand) {
        StatusEdit edit = new StatusEdit();
        switch (brand.getStatus().getType()) {
            case closed:
                edit.setType(StatusEdit.Type.closed);
            case open:
                edit.setType(StatusEdit.Type.open);
        }
        return edit;
    }

    @Nullable
    private AboutEdit mapAbout(Brand brand) {
        if (StringUtils.isAnyBlank(brand.getWebsite(), brand.getDescription())) return null;

        AboutEdit about = new AboutEdit();
        about.setWebsite(brand.getWebsite());
        String description = StringUtils.trimToNull(brand.getDescription());
        if (StringUtils.isNotBlank(description) && description.length() >= 3 && description.length() <= 500) {
            about.setDescription(brand.getDescription());
        }
        return about;
    }

    private MenuEdit mapMenu(Brand brand) {
        MenuEdit edit = new MenuEdit();
        if (brand.getMenu() != null) {
            edit.setUrl(brand.getMenu().getUrl());
        }
        if (brand.getPrice() != null) {
            edit.setPricePerPax(brand.getPrice().getPerPax());
        }

        if (edit.getUrl() == null && edit.getPricePerPax() == null && edit.getImages() == null) return null;
        return edit;
    }
}
