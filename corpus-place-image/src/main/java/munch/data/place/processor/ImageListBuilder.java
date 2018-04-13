package munch.data.place.processor;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 19/12/2017
 * Time: 6:28 PM
 * Project: munch-data
 */
public final class ImageListBuilder {
    private static final Pattern X_PATTERN = Pattern.compile("[xX]");
    private static final Set<String> MUNCH_SOURCE = Set.of("munch-franchise", "munch-concept");

    private List<ProcessedImage> imageList;

    public ImageListBuilder(List<ProcessedImage> imageList) {
        this.imageList = imageList;
    }

    public List<ProcessedImage> select() {
        List<ProcessedImage> images = new ArrayList<>();

        imageList.stream()
                .filter(image -> image.isOutput("food", 0.8f))
                .sorted(Comparator.comparingInt(ImageListBuilder::sortFromSource)
                        .thenComparingLong(ImageListBuilder::sortSize)
                        .thenComparingDouble(ImageListBuilder::sortOutput))
                .limit(3)
                .forEach(images::add);

//        // Image list not empty, Remove all images from munch if first image is not from munch
//        if (!images.isEmpty() && !MUNCH_SOURCE.contains(images.get(0).getImage().getSource())) {
//            // Remove all image that is not created through Munch
//            images.removeIf(image -> MUNCH_SOURCE.contains(image.getImage().getSource()));
//        }

        imageList.stream()
                .filter(image -> image.isOutput("place", 0.8f))
                .sorted(Comparator.comparingInt(ImageListBuilder::sortFromSource)
                        .thenComparingLong(ImageListBuilder::sortSize)
                        .thenComparingDouble(ImageListBuilder::sortOutput))
                .limit(1)
                .forEach(images::add);

        return images;
    }

    /**
     * @param image image
     * @return int for comparing
     */
    private static int sortFromSource(ProcessedImage image) {
        switch (image.getImage().getFrom()) {
            case Instagram:
                return 200;
            case ArticleFullPage:
                return 300;
            case ArticleFullPageDoc:
                return 310;
            case Place:
            default:
                switch (image.getImage().getSource()) {
                    case "munch-place-info":
                    case "munch-ugc":
                        return 100;
                    case "munch-crawler-mall":
                        return 410;
                    case "munch-franchise":
                        return 420;
                    case "munch-crawler":
                        return 600;
                    case "munch-concept":
                        return 700;
                    default:
                        return Integer.MAX_VALUE;
                }
            case ArticleListPage:
                return 500;
        }
    }

    /**
     * @param image image
     * @return float for comparing
     */
    private static float sortOutput(ProcessedImage image) {
        float value = image.getFinnLabel().getMaxOutput().getValue();
        return 1.0f - value;
    }

    private static long sortSize(ProcessedImage image) {
        long size = 0;
        Map<String, String> images = image.getImage().getImages();
        for (String key : images.keySet()) {
            size += parseKey(key);
        }
        return Long.MAX_VALUE - size;
    }

    private static long parseKey(String key) {
        try {
            String[] x = X_PATTERN.split(key);
            if (x.length == 2) return Long.parseLong(x[0]) * Long.parseLong(x[1]);
        } catch (NumberFormatException | NullPointerException ignored) {
        }
        return 0;
    }

    /**
     * Select images
     *
     * @param imageList image list
     * @return List ProcessedImage
     */
    public static List<ProcessedImage> select(List<ProcessedImage> imageList) {
        return new ImageListBuilder(imageList).select();
    }
}
