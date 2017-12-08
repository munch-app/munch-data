package munch.data.place.parser.images;

import corpus.data.CorpusClient;
import corpus.data.CorpusData;
import corpus.field.AbstractKey;
import corpus.field.FieldUtils;
import corpus.images.ImageCachedField;
import munch.data.structure.Place;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 8/12/2017
 * Time: 12:24 PM
 * Project: munch-data
 */
@Singleton
public final class ImagePlaceholderDatabase {
    private static final Logger logger = LoggerFactory.getLogger(ImagePlaceholderDatabase.class);

    private final CorpusClient corpusClient;
    private List<ImageGroup> imageGroups;

    @Inject
    public ImagePlaceholderDatabase(CorpusClient corpusClient) {
        this.corpusClient = corpusClient;
        sync();

        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        exec.scheduleAtFixedRate(this::sync, 24, 24, TimeUnit.HOURS);
    }

    /**
     * Find place holder image
     *
     * @param tag tag
     * @return List<Place.Image>
     */
    public List<Place.Image> findImages(Place.Tag tag) {
        Set<String> explicits = tag.getExplicits().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        for (ImageGroup imageGroup : imageGroups) {
            if (explicits.contains(imageGroup.tag)) {
                return Collections.singletonList(imageGroup.image);
            }
        }

        return Collections.emptyList();
    }

    private void sync() {
        List<ImageGroup> imageGroups = new ArrayList<>();
        corpusClient.list("Sg.MunchSheet.ImagePlaceholder").forEachRemaining(data -> {
            ImageGroup imageGroup = parseImageGroup(data);
            if (imageGroup != null) {
                imageGroups.add(imageGroup);
            }
        });

        imageGroups.sort(Comparator.comparingInt(o -> o.order));
        this.imageGroups = imageGroups;
    }

    private static final AbstractKey OrderKey = AbstractKey.of("Munch.ImagePlaceholder.order");

    @SuppressWarnings("ConstantConditions")
    @Nullable
    private ImageGroup parseImageGroup(CorpusData data) {
        int order = OrderKey.getValueInt(data, 0);
        String tag = FieldUtils.getValue(data, "Munch.ImagePlaceholder.tag");
        Place.Image imageField = FieldUtils.get(data, "Munch.ImagePlaceholder.image", fields -> {
            if (fields.isEmpty()) return null;
            ImageCachedField field = new ImageCachedField(fields.get(0));
            Place.Image image = new Place.Image();
            image.setWeight(field.getWeight(1.0));
            image.setSource(field.getSource());
            image.setImages(field.getImages());
            return image;
        });

        if (tag == null || imageField == null) return null;
        return new ImageGroup(order, tag, imageField);
    }

    class ImageGroup {
        private int order;
        private String tag;
        private Place.Image image;

        private ImageGroup(int order, String tag, Place.Image image) {
            this.order = order;
            this.tag = tag.toLowerCase();
            this.image = image;
        }
    }
}
