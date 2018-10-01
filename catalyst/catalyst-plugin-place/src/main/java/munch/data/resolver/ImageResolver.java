package munch.data.resolver;

import catalyst.mutation.ImageMutationClient;
import catalyst.mutation.PlaceImageMutation;
import catalyst.mutation.PlaceMutation;
import munch.file.Image;
import munch.file.ImageClient;
import munch.file.ImageMeta;
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
        images.addAll(getImages(mutation, PlaceImageMutation.Type.food, 10));
        images.addAll(getImages(mutation, PlaceImageMutation.Type.place, 2));
        return images;
    }

    private List<? extends Image> getImages(PlaceMutation mutation, PlaceImageMutation.Type type, int size) {
        NextNodeList<PlaceImageMutation> images = imageMutationClient.list(mutation.getPlaceId(), type, null, size);
        if (hasConflict(images)) images.removeIf(pim -> pim.getSource().equals("v2.catalyst.munch.space"));

        List<ImageMeta> imageMetas = images.stream()
                .map(im -> imageClient.get(im.getImageId()))
                .collect(Collectors.toList());

        if (isAnyPNG(imageMetas)) {
            List<ImageMeta> nonPNGImages = imageMetas.stream()
                    .filter(image -> !isPNG(image))
                    .collect(Collectors.toList());

            if (!nonPNGImages.isEmpty()) return nonPNGImages;
        }

        return imageMetas;
    }

    private static boolean hasConflict(List<PlaceImageMutation> images) {
        boolean hasV2 = false, hasOther = false;
        for (PlaceImageMutation image : images) {
            if (image.getSource().equals("v2.catalyst.munch.space")) {
                hasV2 = true;
            } else {
                hasOther = true;
            }
        }
        return hasV2 && hasOther;
    }

    /**
     * deprecate this fast
     */
    private static boolean isAnyPNG(List<ImageMeta> images) {
        for (ImageMeta image : images) {
            if (isPNG(image)) return true;
        }
        return false;
    }

    private static boolean isPNG(ImageMeta image) {
        return image.getMeta() != null && image.getMeta().getContentType().equals("image/png");
    }
}
