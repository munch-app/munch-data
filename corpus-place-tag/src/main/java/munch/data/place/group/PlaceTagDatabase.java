package munch.data.place.group;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableRecord;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 8/3/18
 * Time: 5:04 PM
 * Project: munch-data
 */
@SuppressWarnings("Guava")
@Singleton
public final class PlaceTagDatabase {
    private static final Logger logger = LoggerFactory.getLogger(PlaceTagDatabase.class);
    private static final int MAX = 1200;

    private final AirtableApi.Table table;

    private final Supplier<List<PlaceTagGroup>> supplier = Suppliers.memoizeWithExpiration(this::loadAll, 3, TimeUnit.HOURS);
    private final Supplier<BiMap<String, String>> biMapSupplier = Suppliers.memoizeWithExpiration(() -> {
        List<PlaceTagGroup> placeTagGroups = supplier.get();
        BiMap<String, String> map = HashBiMap.create();
        placeTagGroups.forEach(group -> map.put(group.getRecordId(), group.getName().toLowerCase()));
        return map;
    }, 1, TimeUnit.HOURS);

    @Inject
    public PlaceTagDatabase(AirtableApi table) {
        this.table = table.base("appPeSSAEXOQtRTJj").table("Tag Group");
    }

    public List<PlaceTagGroup> getAll() {
        return supplier.get();
    }

    private List<PlaceTagGroup> loadAll() {
        List<PlaceTagGroup> list = new ArrayList<>();
        table.select(Duration.ofSeconds(1)).forEachRemaining(airtableRecord -> {
            list.add(parse(airtableRecord));
        });
        return list;
    }

    private PlaceTagGroup parse(AirtableRecord airtableRecord) {
        String name = airtableRecord.getField("Name").asText();

        PlaceTagGroup group = new PlaceTagGroup(airtableRecord.getId(), name);
        group.setType(airtableRecord.getField("Type").asText());
        group.setOrder(airtableRecord.getField("Order").asDouble(0.0));
        group.setConverts(toLowercaseSet(airtableRecord.getField("Converts")));

        Set<String> synonyms = toLowercaseSet(airtableRecord.getField("Synonyms"));
        synonyms.add(name.toLowerCase());
        group.setSynonyms(synonyms);
        return group;
    }

    private static Set<String> toLowercaseSet(JsonNode node) {
        Set<String> values = new HashSet<>();
        for (JsonNode jsonNode : node) {
            values.add(jsonNode.asText().toLowerCase());
        }
        return values;
    }

    private String findRecordId(String name) {
        List<PlaceTagGroup> list = getAll();
        for (PlaceTagGroup placeTagGroup : list) {
            if (placeTagGroup.getName().equalsIgnoreCase(name)) {
                return placeTagGroup.getRecordId();
            }
        }
        return null;
    }

    public List<String> tagsToIds(List<String> tags) {
        BiMap<String, String> map = biMapSupplier.get().inverse();
        return tags.stream()
                .map(s-> map.get(s.toLowerCase()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<String> idsToTags(List<String> ids) {
        BiMap<String, String> map = biMapSupplier.get();
        return ids.stream()
                .map(s-> map.get(s.toLowerCase()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * This method will update existing list if found
     *
     * @param name          name of tag
     * @param total         total linked to place
     * @param withoutImages total linked to place without any images
     * @param predicted     total linked to place that are predicted
     */
    public void put(String name, int total, int withoutImages, int predicted) {
        AirtableRecord record = new AirtableRecord();
        record.setId(findRecordId(name));

        if (record.getId() != null) {
            record.setFields(Map.of(
                    "Total Count", JsonUtils.toTree(total),
                    "Without Image Count", JsonUtils.toTree(withoutImages),
                    "Predicted Count", JsonUtils.toTree(predicted)
            ));
            table.patch(record);
            return;
        }

        List<PlaceTagGroup> all = getAll();
        if (all.size() >= MAX) {
            logger.warn("There is equal or more then 1200 tags");
            return;
        }

        record.setFields(Map.of(
                "Name", JsonUtils.toTree(WordUtils.capitalizeFully(name)),
                "Total Count", JsonUtils.toTree(total),
                "Without Image Count", JsonUtils.toTree(withoutImages),
                "Predicted Count", JsonUtils.toTree(predicted)
        ));
        AirtableRecord postedRecord = table.post(record);
        all.add(parse(postedRecord));
    }
}
