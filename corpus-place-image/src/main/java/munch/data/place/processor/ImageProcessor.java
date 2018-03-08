package munch.data.place.processor;

import catalyst.utils.exception.ExceptionRetriable;
import catalyst.utils.exception.Retriable;
import com.fasterxml.jackson.databind.JsonNode;
import corpus.data.DocumentClient;
import munch.data.place.collector.CollectedImage;
import munch.finn.FinnClient;
import munch.finn.FinnLabel;
import munch.finn.RawLabelException;
import munch.restful.core.JsonUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 4:43 PM
 * Project: munch-data
 */
@Singleton
public final class ImageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ImageProcessor.class);
    private static final String HASH_KEY = "Sg.Munch.Place.Image.Finn-0.4.0";
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
        return collectedImages.stream()
                .map(this::parse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * @param collectedImage collected image
     * @return parsed ProcessedI?mage
     */
    private ProcessedImage parse(CollectedImage collectedImage) {
        FinnLabel finnLabel = cachedPredict(collectedImage);
        if (finnLabel == null) return null;

        ProcessedImage processedImage = new ProcessedImage();
        processedImage.setImage(collectedImage);
        processedImage.setFinnLabel(finnLabel);
        return processedImage;
    }

    private FinnLabel cachedPredict(CollectedImage collectedImage) {
        String source = Objects.requireNonNull(collectedImage.getSource());
        String imageKey = Objects.requireNonNull(collectedImage.getImageKey());

        JsonNode cache = documentClient.get(HASH_KEY, source, imageKey);
        if (cache != null) return JsonUtils.toObject(cache, FinnLabel.class);

        FinnLabel finnLabel = predict(collectedImage);
        if (finnLabel == null) return null;

        documentClient.put(HASH_KEY, source, imageKey, JsonUtils.toTree(finnLabel));
        return finnLabel;
    }

    /**
     * @param collectedImage image to select url from
     * @return FinnLabel selected for prediction
     */
    private FinnLabel predict(CollectedImage collectedImage) {
        if (collectedImage.getImages() == null || collectedImage.getImages().isEmpty()) {
            logger.warn("Images not suppose to be empty, CollectedImage: {}", collectedImage);
            throw new IllegalStateException("images is empty");
        }

        String imageUrl = collectedImage.getImages().get("640x640");
        if (imageUrl == null) imageUrl = collectedImage.getImages().get("original");
        if (imageUrl == null) imageUrl = collectedImage.getImages().entrySet().iterator().next().getValue();

        File file = null;
        try {
            URL url = new URL(imageUrl);
            file = File.createTempFile(FilenameUtils.getName(url.getPath()), "");

            File finalFile = file;
            return retriable.loop(() -> {
                FileUtils.copyURLToFile(url, finalFile);
                try {
                    return finnClient.predict(finalFile);
                } catch (RawLabelException e) {
                    logger.warn("RawLabelException for imageUrl: {}", url.toString(), e);
                    return null;
                }
            });
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } finally {
            FileUtils.deleteQuietly(file);
        }
    }
}
