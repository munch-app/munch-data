package munch.data.resolver;

import catalyst.mutation.ImageMutationClient;
import catalyst.mutation.MutationField;
import catalyst.mutation.PlaceImageMutation;
import catalyst.mutation.PlaceMutation;
import munch.data.place.Place;
import munch.file.Image;
import munch.file.ImageClient;
import munch.restful.core.NextNodeList;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 8/8/18
 * Time: 4:35 PM
 * Project: munch-data
 */
@Singleton
public final class MenuResolver {
    private final ImageMutationClient imageMutationClient;
    private final ImageClient imageClient;

    @Inject
    public MenuResolver(ImageMutationClient imageMutationClient, ImageClient imageClient) {
        this.imageMutationClient = imageMutationClient;
        this.imageClient = imageClient;
    }

    public Place.Menu resolve(PlaceMutation mutation) {
        String imageUrl = getMenuUrl(mutation);
        List<Image> images = getImages(mutation);

        if (StringUtils.isBlank(imageUrl) && images.isEmpty()) return null;

        Place.Menu menu = new Place.Menu();
        menu.setImages(images);
        menu.setUrl(imageUrl);
        return menu;
    }

    private List<Image> getImages(PlaceMutation mutation) {
        String placeId = mutation.getPlaceId();
        NextNodeList<PlaceImageMutation> images = imageMutationClient.list(placeId, PlaceImageMutation.Type.menu, null, 4);
        return images.stream()
                .map(im -> imageClient.get(im.getImageId()))
                .collect(Collectors.toList());
    }

    private String getMenuUrl(PlaceMutation mutation) {
        @Valid List<MutationField<String>> fields = mutation.getMenuUrl();
        if (fields.isEmpty()) return null;
        return StringUtils.trimToNull(fields.get(0).getValue());
    }
}
