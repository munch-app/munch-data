package munch.data.place.processor;

import munch.data.clients.PlaceCardClient;
import munch.data.structure.PlaceJsonCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    private static final String MENU_CARD_ID = "vendor_MenuImage_20171219";

    private final PlaceCardClient cardClient;

    @Inject
    public MenuProcessor(PlaceCardClient cardClient) {
        this.cardClient = cardClient;
    }

    public void put(String placeId, List<ProcessedImage> processedImages) {
        List<ImageMenu> menus = processedImages.stream()
                .filter(image -> image.isOutput("menu", 0.95f))
                .sorted(Comparator.comparingLong(ImageListBuilder::sortSize)
                        .thenComparingDouble(ImageListBuilder::sortOutput))
                .limit(10)
                .map(image -> {
                    ImageMenu menu = new ImageMenu();
                    menu.setImages(image.getImage().getImages());
                    return menu;
                }).collect(Collectors.toList());

        if (menus.isEmpty()) {
            cardClient.deleteIfNonNull(placeId, MENU_CARD_ID);
        } else {
            // Put if card content changes
            logger.info("Menus: {}, Added for {}", menus.size(), placeId);
            PlaceJsonCard card = new PlaceJsonCard(MENU_CARD_ID, menus);
            cardClient.putIfChange(placeId, card);
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
