package munch.data.place;

import com.fasterxml.jackson.databind.JsonNode;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableMapper;
import corpus.airtable.field.*;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import corpus.images.ImageCachedClient;
import munch.data.hour.HourExtractor;
import munch.data.location.PostalParser;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 10/3/2018
 * Time: 7:13 PM
 * Project: munch-data
 */
@Singleton
public final class SuggestMapper extends AirtableMapper {

    private static final HourExtractor hourExtractor = new HourExtractor();

    @Inject
    public SuggestMapper(AirtableApi api, ImageCachedClient imageClient) {
        super(api.base("appU593OcXcCeXeen").table("Place"), getMapper(imageClient), null);
    }

    private static Map<String, FieldMapper> getMapper(ImageCachedClient imageClient) {
        Map<String, FieldMapper> map = new HashMap<>();
        map.put("Place.name", DefaultMapper.INSTANCE);
        map.put("Place.status", SuggestMapper::parseStatus);
        map.put("Endorsed By", RenameMapper.to("Suggest.endorsedBy", new CollaboratorMapper()));

        map.put("Place.image", new AttachmentMapper(imageClient));
        map.put("Place.rawHours", SuggestMapper::parseHours);
        map.put("Place.tag", DefaultMapper.INSTANCE);
        map.put("Place.price", DefaultMapper.INSTANCE);
        map.put("Place.phone", DefaultMapper.INSTANCE);
        map.put("Place.website", DefaultMapper.INSTANCE);
        map.put("Place.description", DefaultMapper.INSTANCE);

        map.put("Place.Location.latLng", DefaultMapper.INSTANCE);
        map.put("Place.Location.address", SuggestMapper::parseAddress);
        map.put("Place.id", DefaultMapper.INSTANCE);
        return map;
    }

    private static List<CorpusData.Field> parseAddress(String key, JsonNode value) {
        String text = value.asText();
        if (StringUtils.isBlank(text)) return List.of();

        String postal = PostalParser.parse(text);
        if (postal == null) return List.of();
        return List.of(PlaceKey.Location.address.createField(text), PlaceKey.Location.postal.createField(postal));
    }

    private static List<CorpusData.Field> parseHours(String key, JsonNode value) {
        String text = value.asText();
        if (StringUtils.isBlank(text)) return List.of();

        return hourExtractor.extract(text).stream()
                .map(h -> PlaceKey.Hour.create(h.getDay().name(), h.getOpen(), h.getClose()))
                .collect(Collectors.toList());
    }

    private static List<CorpusData.Field> parseStatus(String key, JsonNode value) {
        switch (value.asText("").toLowerCase()) {
            case "open":
                return List.of(new CorpusData.Field(key, "open"));
            case "permanently closed":
                return List.of(new CorpusData.Field(key, "close"));
            case "does not exist":
                return List.of(new CorpusData.Field(key, "delete"));
            case "not food place":
                return List.of(new CorpusData.Field(key, "delete"));
            default:
                return List.of();
        }
    }
}
