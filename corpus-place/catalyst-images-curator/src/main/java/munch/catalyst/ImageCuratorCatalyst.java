package munch.catalyst;

import catalyst.ReactiveEngine;
import catalyst.data.CatalystClient;
import catalyst.data.DataClient;
import catalyst.utils.FieldCollector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import corpus.data.CorpusData;
import corpus.field.NativeKey;
import corpus.field.PlaceKey;
import corpus.utils.FieldUtils;
import munch.catalyst.sources.ImageWhitelistSource;
import munch.catalyst.sources.SourcedImage;
import munch.corpus.docs.SheetNotFound;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 4/5/2017
 * Time: 10:07 PM
 * Project: munch-corpus
 */
@Singleton
public class ImageCuratorCatalyst extends ReactiveEngine {
    private static final Logger logger = LoggerFactory.getLogger(ImageCuratorCatalyst.class);

    private final ImageWhitelistSource whitelistSource;
    private final ObjectMapper objectMapper;

    @Inject
    public ImageCuratorCatalyst(DataClient dataClient, CatalystClient catalystClient, ImageWhitelistSource whitelistSource, ObjectMapper objectMapper) {
        super(logger, dataClient, catalystClient);
        this.whitelistSource = whitelistSource;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void preStart() {
        super.preStart();
        try {
            whitelistSource.sync();
        } catch (IOException | SheetNotFound e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected CorpusData process(String catalystId, @Nullable CorpusData data) {
        FieldCollector typeCollector = new FieldCollector(PlaceKey.type, NativeKey.type);
        List<SourcedImage> images = new ArrayList<>();
        dataClient.getLinked(catalystId).forEachRemaining(corpusData -> {
            typeCollector.add(corpusData);
            images.addAll(whitelistSource.collect(corpusData));
        });

        // No place data found in type
        if (!typeCollector.collect().contains("place")) {
            return null;
        }

        if (data == null) {
            data = newCorpusData(catalystId);
            data.put(ImageCuratorKey.refreshedMillis, 0);
        }

        process(data, images);
        return data;
    }

    /**
     * @param data   corpus data to edit
     * @param images images to add or update
     */
    private void process(CorpusData data, List<SourcedImage> images) {
        if (ImageCuratorKey.refreshedMillis.afterDays(data, 7)) {
            // Refresh all images after 7 days, remove and re-add all
            data.getFields().removeIf(field -> ImageCuratorKey.image.getKey().equals(field.getKey()));
            images.forEach(image -> data.put(nextImage(image)));
            FieldUtils.removeAll(data, ImageCuratorKey.refreshedMillis.getKey());
            data.put(ImageCuratorKey.refreshedMillis, System.currentTimeMillis());
        } else {
            // Remove not inside images
            Set<String> imageKeys = images.stream().map(SourcedImage::getImageKey)
                    .collect(Collectors.toSet());
            Set<String> existingKeys = new HashSet<>();
            data.getFields().removeIf(field -> {
                if (ImageCuratorKey.image.isField(field)) {
                    String key = ImageCuratorKey.image.getImageKey(field);
                    existingKeys.add(key);
                    return !imageKeys.contains(key);
                }
                return false;
            });

            // Append any other new images
            for (SourcedImage image : images) {
                if (!existingKeys.contains(image.getImageKey())) {
                    data.putField(newImage(image, 0));
                }
            }
        }
    }

    private CorpusData.Field nextImage(SourcedImage image) {
        double randomBase = RandomUtils.nextDouble(0, Integer.MAX_VALUE);
        long random = (long) (randomBase * image.getBoost());
        return newImage(image, random);
    }

    private CorpusData.Field newImage(SourcedImage image, long random) {
        CorpusData.Field field = new CorpusData.Field();
        field.setKey(ImageCuratorKey.image.getKey());
        field.setValue(Long.toString(random));
        field.setPositive(true);

        Map<String, String> metadata = new HashMap<>();
        // Created image is parsed as long in millis
        metadata.put("imageSource", image.getSource());
        metadata.put("imageKey", image.getImageKey());
        try {
            metadata.put("images", objectMapper.writeValueAsString(image.getImages()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        field.setMetadata(metadata);
        return field;
    }
}
