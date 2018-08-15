package munch.data.resolver;

import catalyst.mutation.ImageMutationClient;
import catalyst.mutation.PlaceImageMutation;
import catalyst.mutation.PlaceMutation;
import munch.file.Image;
import munch.file.ImageClient;
import munch.restful.core.NextNodeList;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 8/8/18
 * Time: 4:34 PM
 * Project: munch-data
 */
@Singleton
public final class ImageResolver {
    private final ImageMutationClient imageMutationClient;
    private final ImageClient imageClient;

    @Inject
    public ImageResolver(ImageMutationClient imageMutationClient, ImageClient imageClient) {
        this.imageMutationClient = imageMutationClient;
        this.imageClient = imageClient;
    }

    public List<Image> resolve(PlaceMutation mutation) {
        List<Image> images = new ArrayList<>();
        images.addAll(getImages(mutation, PlaceImageMutation.Type.food, 4));
        images.addAll(getImages(mutation, PlaceImageMutation.Type.place, 1));
        return images;
    }

    private List<Image> getImages(PlaceMutation mutation, PlaceImageMutation.Type type, int size) {
        String placeId = mutation.getPlaceId();
        NextNodeList<PlaceImageMutation> images = imageMutationClient.list(placeId, type, null, size);
        return images.stream()
                .map(im -> imageClient.get(im.getImageId()))
                .collect(Collectors.toList());
    }
}
