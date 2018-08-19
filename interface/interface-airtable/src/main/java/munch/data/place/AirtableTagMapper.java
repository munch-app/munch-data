package munch.data.place;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableRecord;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 19/8/18
 * Time: 11:19 PM
 * Project: munch-data
 */
@Singleton
public final class AirtableTagMapper {
    private static final Logger logger = LoggerFactory.getLogger(AirtableTagMapper.class);

    private final AirtableApi.Table tagTable;

    private final Map<String, Pair<String, Place.Tag>> tagMap = new HashMap<>();

    @Inject
    public AirtableTagMapper(AirtableApi api) {
        this.tagTable = api.base("appDcx5b3vgkhcYB5").table("Tag");
    }

    public JsonNode mapField(List<Place.Tag> tags) {
        ArrayNode fields = JsonUtils.createArrayNode();
        for (Place.Tag tag : tags) {
            fields.add(tagMap.computeIfAbsent(tag.getTagId(), s -> find(tag)).getLeft());
        }
        return fields;
    }

    private Pair<String, Place.Tag> find(Place.Tag tag) {
        List<AirtableRecord> records = tagTable.find("tagId", tag.getTagId());

        AirtableRecord record;
        if (records.size() == 0) record = new AirtableRecord();
        else record = records.get(0);

        for (int i = 1; i < records.size(); i++) {
            // Auto Cleanup
            tagTable.delete(records.get(i).getId());
            sleep();
        }

        if (equals(tag, record)) return Pair.of(record.getId(), tag);

        record.setFields(new HashMap<>());
        record.putField("tagId", tag.getTagId());
        record.putField("name", tag.getName());
        record.putField("type", tag.getType().name());

        if (record.getId() == null) {
            record = tagTable.post(record);
        } else {
            record = tagTable.patch(record);
        }

        logger.info("Updated Tag: {}", tag.getTagId());
        sleep();

        return Pair.of(record.getId(), tag);
    }

    private static boolean equals(Place.Tag tag, AirtableRecord record) {
        if (!record.getField("tagId").asText().equals(tag.getTagId())) return false;
        if (!record.getField("name").asText().equals(tag.getName())) return false;
        if (!record.getField("type").asText().equals(tag.getType().name())) return false;
        return true;
    }

    private static void sleep() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
