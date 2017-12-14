package munch.data.place.processor;

import catalyst.utils.exception.ExceptionRetriable;
import catalyst.utils.exception.Retriable;
import com.fasterxml.jackson.databind.JsonNode;
import corpus.data.DocumentClient;
import munch.data.place.collector.CollectedImage;
import munch.finn.FinnClient;
import munch.finn.FinnLabel;
import munch.restful.core.JsonUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 4:43 PM
 * Project: munch-data
 */
@Singleton
public final class ImageProcessor {
    private static final String HASH_KEY = "Sg.Munch.PlaceImage.Finn-0.4.0";
    private static final Retriable retriable = new ExceptionRetriable(10);

    private final FinnClient finnClient;
    private final DocumentClient documentClient;

    @Inject
    public ImageProcessor(FinnClient finnClient, DocumentClient documentClient) {
        this.finnClient = finnClient;
        this.documentClient = documentClient;
    }

    /**
     * Future Implementation need to check for text
     *
     * @param collectedImages collected image for processing
     * @return selected image
     */
    public List<ProcessedImage> process(List<CollectedImage> collectedImages) {
        List<ProcessedImage> processedImages = collectedImages.stream()
                .map(this::parse)
                .collect(Collectors.toList());

        List<ProcessedImage> finalList = new ArrayList<>();

        // Select 3 food image, Sorted by Place.image, then score
        processedImages.stream()
                .filter(image -> image.getFinnLabel().getMaxOutput().getKey().equals("food"))
                .sorted(Comparator.comparingInt(ImageProcessor::sortFrom)
                        .thenComparingLong(ImageProcessor::sortSize)
                        .thenComparingDouble(ImageProcessor::sortOutput)
                ).limit(3)
                .forEach(finalList::add);


        // Select 1 place image, Sorted by Place.image, then score
        processedImages.stream()
                .filter(image -> image.getFinnLabel().getMaxOutput().getKey().equals("place"))
                .sorted(Comparator.comparingInt(ImageProcessor::sortFrom)
                        .thenComparingLong(ImageProcessor::sortSize)
                        .thenComparingDouble(ImageProcessor::sortOutput)
                ).limit(1)
                .forEach(finalList::add);

        return finalList;
    }

    /**
     * @param collectedImage collected image
     * @return parsed ProcessedI?mage
     */
    private ProcessedImage parse(CollectedImage collectedImage) {
        String uniqueId = Objects.requireNonNull(collectedImage.getUniqueId());
        JsonNode cache = documentClient.get(HASH_KEY, uniqueId);
        if (cache != null) return JsonUtils.toObject(cache, ProcessedImage.class);

        ProcessedImage processedImage = new ProcessedImage();
        processedImage.setImage(collectedImage);
        processedImage.setFinnLabel(predict(collectedImage));
        documentClient.put(HASH_KEY, uniqueId, JsonUtils.toTree(processedImage));
        return processedImage;
    }

    /**
     * @param collectedImage image to select url from
     * @return FinnLabel selected for prediction
     */
    private FinnLabel predict(CollectedImage collectedImage) {
        assert collectedImage.getImages().isEmpty() : "Images not suppose to be empty";
        String imageUrl = collectedImage.getImages().get("640x640");
        if (imageUrl == null) imageUrl = collectedImage.getImages().get("original");
        if (imageUrl == null) imageUrl = collectedImage.getImages().entrySet().iterator().next().getValue();

        File file = null;
        try {
            URL url = new URL(imageUrl);
            file = File.createTempFile("", FilenameUtils.getName(url.getPath()));

            File finalFile = file;
            return retriable.loop(() -> {
                FileUtils.copyURLToFile(url, finalFile);
                return finnClient.predict(finalFile);
            });
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } finally {
            FileUtils.deleteQuietly(file);
        }
    }

    /**
     * @param image image
     * @return int for comparing
     */
    private static int sortFrom(ProcessedImage image) {
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
    private static float sortOutput(ProcessedImage image) {
        return image.getFinnLabel().getMaxOutput().getValue();
    }

    private static long sortSize(ProcessedImage image) {
        for (Map.Entry<String, String> entry : image.getImage().getImages().entrySet()) {
            switch (entry.getKey().toLowerCase()) {
                case "original":
                    return 200 * 200;
                case "150x150":
                    return 150 * 150;
                case "320x320":
                    return 320 * 320;
                case "640x640":
                    return 640 * 640;
                case "1080x1080":
                    return 1080 * 1080;
                default:
                    String[] leftRight = entry.getKey().split("x");
                    return Long.parseLong(leftRight[0]) * Long.parseLong(leftRight[1]);
            }
        }
        return 0;
    }
}
