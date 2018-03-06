package munch.data.place.processor;

import munch.data.extended.ExtendedDataSync;
import munch.data.extended.PlaceMenu;
import munch.data.extended.PlaceMenuClient;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 19/12/2017
 * Time: 8:32 PM
 * Project: munch-data
 */
@Singleton
public final class MenuProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MenuProcessor.class);
//    private static final String MENU_CARD_ID = "vendor_MenuImage_20171219"; OLD CARD ID

    private final ExtendedDataSync<PlaceMenu> dataSync;

    @Inject
    public MenuProcessor(PlaceMenuClient placeMenuClient) {
        this.dataSync = new ExtendedDataSync<>(Duration.ofSeconds(1), placeMenuClient);
    }

    public void put(String placeId, List<ProcessedImage> processedImages) {
        List<PlaceMenu> menus = processedImages.stream()
                .filter(image -> image.isOutput("menu", 0.91f))
                .map(this::parse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        dataSync.sync(placeId, menus);
        if (!menus.isEmpty()) {
            logger.info("Menus: {}, Added for {}", menus.size(), placeId);
        }
    }

    private PlaceMenu parse(ProcessedImage processedImage) {
        Map<String, String> images = processedImage.getImage().getImages();
        if (images.isEmpty()) return null;

        PlaceMenu menu = new PlaceMenu();
        menu.setSortKey(getSortKey(processedImage));

        menu.setType(PlaceMenu.TYPE_IMAGE);
        menu.setThumbnail(images);
        menu.setUrl(getLargestImage(images));

        menu.setSource(processedImage.getImage().getSource());
        menu.setSourceName(processedImage.getImage().getSourceName());
        menu.setSourceId(processedImage.getImage().getSourceId());
        return menu;
    }

    private String getSortKey(ProcessedImage processedImage) {
        Map.Entry<String, Float> max = processedImage.getFinnLabel().getMaxOutput();
        String source = Objects.requireNonNull(processedImage.getImage().getSource());
        String imageKey = Objects.requireNonNull(processedImage.getImage().getImageKey());
        if (max.getValue() > 99) return "99_" + source + "_" + imageKey;

        int num = max.getValue().intValue();
        if (num < 0) return "00_" + source + "_" + imageKey;

        if (num % 10 == 0) return "0" + num + "_" + source + "_" + imageKey;
        return num + "_" + source + "_" + imageKey;
    }

    private String getLargestImage(Map<String, String> images) {
        String url = images.get("original");
        if (url != null) return url;

        return images.entrySet().stream()
                .map(entry -> Pair.of(getDimension(entry.getKey()), entry.getValue()))
                .max(Comparator.comparingInt(Pair::getLeft))
                .map(Pair::getRight)
                .orElseGet(() -> images.values().iterator().next());
    }

    private int getDimension(String key) {
        String[] wh = key.split("[xX]");
        try {
            return Integer.parseInt(wh[0]) * Integer.parseInt(wh[1]);
        } catch (NumberFormatException | NullPointerException | IndexOutOfBoundsException e) {
            return 0;
        }
    }

    public static class ImageMenu {
        private Map<String, String> images;

        public Map<String, String> getImages() {
            return images;
        }

        public void setImages(Map<String, String> images) {
            this.images = images;
        }
    }
}
