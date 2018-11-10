package munch.data.airtable;

import com.fasterxml.jackson.databind.JsonNode;
import edit.utils.hour.HourExtractor;
import edit.utils.hour.HourNormaliser;
import edit.utils.hour.OpenHour;
import munch.data.Hour;
import munch.file.Image;
import munch.file.ImageClient;
import munch.restful.core.exception.UnknownException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 5/6/18
 * Time: 12:20 PM
 * Project: munch-data
 */
public final class AirtableUtils {
    private static final Logger logger = LoggerFactory.getLogger(AirtableUtils.class);

    private static final HourExtractor HOUR_EXTRACTOR = new HourExtractor();
    private static final HourNormaliser HOUR_NORMALISER = new HourNormaliser();

    public static List<Hour> parseHours(JsonNode node) {
        String value = node.asText();
        if (StringUtils.isBlank(value)) return List.of();

        List<OpenHour> hours = HOUR_EXTRACTOR.extract(value);
        return HOUR_NORMALISER.normalise(hours, (day, open, close) -> {
            Hour hour = new Hour();
            hour.setDay(Hour.Day.valueOf(day));
            hour.setOpen(open);
            hour.setClose(close);
            return hour;
        });
    }


    public static List<Image> getImages(ImageClient imageClient, JsonNode images) {
        try {
            List<Image> imageList = new ArrayList<>();
            for (JsonNode image : images) {
                String url = image.path("url").asText();
                if (StringUtils.isNotBlank(url)) {
                    imageList.add(imageClient.uploadUrl(url, null, null));
                }
            }
            return imageList;
        } catch (UnknownException e) {
            logger.warn("Image failed. {}", images, e);
            return List.of();
        }
    }

    /**
     * @param node text node
     * @return Set of String or Empty Set
     */
    public static Set<String> multiLineToSet(JsonNode node) {
        String text = node.asText();
        if (StringUtils.isBlank(text)) return Set.of();

        return Arrays.stream(text.split("\n"))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
    }
}
