package munch.data.place;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Joiner;
import corpus.airtable.AirtableRecord;
import munch.file.Image;
import munch.restful.core.JsonUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 24/7/18
 * Time: 2:06 PM
 * Project: munch-data
 */
@Singleton
public final class AirtablePlaceMapper {

    private final AirtableMapper airtableMapper;

    @Inject
    public AirtablePlaceMapper(AirtableMapper airtableMapper) {
        this.airtableMapper = airtableMapper;
    }

    public AirtableRecord parse(Place place) {
        AirtableRecord record = new AirtableRecord();
        record.setFields(new HashMap<>());
        record.putField("placeId", place.getPlaceId());
        record.putField("status", place.getStatus().getType().name());

        record.putField("name", place.getName());
        record.putField("names", Joiner.on("\n").join(place.getNames()));
        record.putField("tags", airtableMapper.mapTagField(place.getTags()));

        record.putField("phone", place.getPhone());
        record.putField("website", place.getWebsite());
        record.putField("description", place.getDescription());

        record.putField("location.address", place.getLocation().getAddress());
        record.putField("location.street", place.getLocation().getStreet());
        record.putField("location.unitNumber", place.getLocation().getUnitNumber());
        record.putField("location.neighbourhood", place.getLocation().getNeighbourhood());

        record.putField("location.city", place.getLocation().getCity());
        record.putField("location.country", place.getLocation().getCountry());
        record.putField("location.postcode", place.getLocation().getPostcode());

        record.putField("location.latLng", place.getLocation().getLatLng());

        record.putField("menu.url", () -> JsonUtils.toTree(place.getMenu().getUrl()));
        record.putField("price.perPax", () -> JsonUtils.toTree(place.getPrice().getPerPax()));


        record.putField("hours", () -> {
            String hours = place.getHours().stream()
                    .map(h -> h.getDay().name() + ": " + h.getOpen() + "-" + h.getClose())
                    .collect(Collectors.joining("\n"));
            return JsonUtils.toTree(hours);
        });

        record.putField("images", () -> {
            if (place.getImages().isEmpty()) return JsonUtils.createArrayNode();
            ArrayNode array = JsonUtils.createArrayNode();
            for (Image image : place.getImages()) {
                String url = image.getSizes()
                        .stream()
                        .min(Comparator.comparingInt(Image.Size::getWidth))
                        .map(Image.Size::getUrl)
                        .orElse(null);
                if (url == null) continue;
                array.addObject().put("url", url);
            }
            return array;
        });

        // Linked Data

        record.putField("areas", airtableMapper.mapAreaField(place.getAreas()));

        record.putFieldDate("createdMillis", place.getCreatedMillis());
        record.putFieldDate("updatedMillis", place.getUpdatedMillis());
        record.putField("ranking", place.getRanking());
        return record;
    }

}
