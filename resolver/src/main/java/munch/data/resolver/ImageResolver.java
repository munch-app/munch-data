package munch.data.resolver;

import catalyst.mutation.ImageMutationClient;
import catalyst.mutation.PlaceImageMutation;
import catalyst.mutation.PlaceMutation;
import com.google.common.collect.Iterators;
import munch.file.Image;
import munch.file.ImageClient;
import munch.file.ImageMeta;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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
        Image image = getImage(mutation, PlaceImageMutation.Type.forceFood);
        if (image != null) return List.of(image);

        image = getImage(mutation, PlaceImageMutation.Type.food);
        if (image != null) return List.of(image);

        Iterator<ImageMeta> iterator = iterator(mutation.getPlaceId(), PlaceImageMutation.Type.food);
        Image next = Iterators.getNext(iterator, null);

        if (next != null) return List.of(next);
        return List.of();
    }

    private Image getImage(PlaceMutation mutation, PlaceImageMutation.Type type) {
        Iterator<ImageMeta> iterator = iterator(mutation.getPlaceId(), type);

        return Iterators.find(iterator, image -> {
            if (image == null) return false;
            if (isPNG(image)) return false;
            return true;
        }, null);
    }

    private Iterator<ImageMeta> iterator(String placeId, PlaceImageMutation.Type type) {
        Iterator<PlaceImageMutation> iterator = imageMutationClient.iterator(placeId, type, 10);

        Iterator<ImageMeta> images = Iterators.transform(iterator, im -> {
            if (im == null) return null;
            return imageClient.get(im.getImageId());
        });
        return Iterators.filter(images, Objects::nonNull);
    }

    private static boolean isPNG(ImageMeta image) {
        return image.getMeta() != null && image.getMeta().getContentType().equals("image/png");
    }
}
