package munch.data.place.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by: Fuxing
 * Date: 19/12/2017
 * Time: 6:28 PM
 * Project: munch-data
 */
public final class ImageListBuilder {
    private List<ProcessedImage> imageList;

    private List<ProcessedImage> finalList = new ArrayList<>();

    public ImageListBuilder(List<ProcessedImage> imageList) {
        this.imageList = imageList;
    }

    public void supply(Predicate<List<ProcessedImage>> predicate, Function<Stream<ProcessedImage>, Stream<ProcessedImage>> function) {
        if (predicate.test(finalList)) {
            supply(function);
        }
    }

    public void supply(Function<Stream<ProcessedImage>, Stream<ProcessedImage>> function) {
        function.apply(imageList.stream()).forEach(image -> finalList.add(image));
    }

    public List<ProcessedImage> collect() {
        return finalList;
    }

    /**
     * @param image image
     * @return int for comparing
     */
    public static int sortFrom(ProcessedImage image) {
        switch (image.getImage().getFrom()) {
            case Place:
                return 0;
            case Instagram:
                return 1;
            case Article:
                return 2;
            default:
                return 10;
        }
    }

    /**
     * @param image image
     * @return float for comparing
     */
    public static float sortOutput(ProcessedImage image) {
        float value = image.getFinnLabel().getMaxOutput().getValue();
        return 1.0f - value;
    }

    public static long sortSize(ProcessedImage image) {
        Map<String, String> images = image.getImage().getImages();
        if (images.containsKey("1080x1080")) return 0;
        if (images.containsKey("640x640")) return 1;
        if (images.containsKey("320x320")) return 2;
        if (images.containsKey("150x150")) return 3;
        return 10;
    }
}
