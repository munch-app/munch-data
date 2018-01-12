package munch.data.place.parser;

import corpus.data.CorpusClient;
import corpus.data.CorpusData;
import corpus.field.AbstractKey;
import corpus.field.FieldUtils;
import corpus.images.ImageField;
import munch.data.structure.Place;
import munch.data.structure.SourcedImage;
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
 * Date: 14/10/2017
 * Time: 10:13 PM
 * Project: munch-data
 */
@Singleton
public final class ImageParser extends AbstractParser<List<SourcedImage>> {

    private final Placeholder placeholder;

    @Inject
    public ImageParser(Placeholder placeholder) {
        this.placeholder = placeholder;
    }

    /**
     * @param list list of corpus
     * @return List of Place.Image can be empty
     */
    @Override
    public List<SourcedImage> parse(Place place, List<CorpusData> list) {
        List<SourcedImage> collected = collect(list);
        if (!collected.isEmpty()) return collected;

        return placeholder.findImages(place.getTag());
    }

    private List<SourcedImage> collect(List<CorpusData> list) {
        return list.stream()
                .filter(data -> data.getCorpusName().equals("Sg.Munch.PlaceImage"))
                .flatMap(data -> data.getFields().stream())
                .filter(field -> field.getKey().equals("Place.image"))
                .sorted(Comparator.comparingInt(field -> Integer.parseInt(field.getValue())))
                .map(ImageField::new)
                .map(field -> {
                    SourcedImage image = new SourcedImage();
                    image.setSource(field.getSource());
                    image.setSourceId(field.getSourceId());
                    image.setSourceName(field.getSourceName());
                    image.setImages(field.getImages());
                    return image;
                }).collect(Collectors.toList());
    }

    /**
     * Created by: Fuxing
     * Date: 8/12/2017
     * Time: 12:24 PM
     * Project: munch-data
     */
    @Singleton
    public static final class Placeholder {
        private static final Logger logger = LoggerFactory.getLogger(Placeholder.class);

        private final CorpusClient corpusClient;
        private List<ImageGroup> imageGroups;

        @Inject
        public Placeholder(CorpusClient corpusClient) {
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
        public List<SourcedImage> findImages(Place.Tag tag) {
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
            SourcedImage imageField = FieldUtils.get(data, "Munch.ImagePlaceholder.image", fields -> {
                if (fields.isEmpty()) return null;
                ImageField field = new ImageField(fields.get(0));
                SourcedImage image = new SourcedImage();
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
            private SourcedImage image;

            private ImageGroup(int order, String tag, SourcedImage image) {
                this.order = order;
                this.tag = tag.toLowerCase();
                this.image = image;
            }
        }
    }
}
