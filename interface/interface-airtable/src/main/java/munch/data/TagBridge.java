package munch.data;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableRecord;
import munch.data.airtable.AirtableBridge;
import munch.data.client.ElasticClient;
import munch.data.client.TagClient;
import munch.data.elastic.ElasticUtils;
import munch.data.tag.Tag;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Created by: Fuxing
 * Date: 6/6/18
 * Time: 3:01 PM
 * Project: munch-data
 */
@Singleton
public final class TagBridge extends AirtableBridge<Tag> {
    private static final Logger logger = LoggerFactory.getLogger(TagBridge.class);

    private final TagClient client;
    private final ElasticClient elasticClient;

    @Inject
    public TagBridge(AirtableApi airtableApi, TagClient client, ElasticClient elasticClient) {
        super(logger, airtableApi.base("appERO4wuQ5oJSTxO").table("Tag"), client::iterator);
        this.client = client;
        this.elasticClient = elasticClient;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(8);
    }

    @Override
    protected void processServer(Tag data) {
        List<AirtableRecord> records = table.find("tagId", data.getTagId());
        if (records.size() == 1) return;
        if (records.size() > 1) throw new IllegalStateException("More then 1 Tag with the same id.");

        // No Records found: Delete
        client.delete(data.getTagId());
    }

    @Override
    protected void processAirtable(AirtableRecord record, Tag updated) {
        String tagId = record.getField("tagId").asText();
        if (StringUtils.isNotBlank(tagId)) {
            // Update if Changed
            updated.setCounts(getCount(updated));
            if (updated.equals(client.get(tagId))) return;

            AirtableRecord patch = new AirtableRecord();
            patch.setId(record.getId());
            patch.putField("counts.total", updated.getCounts().getTotal());

            // Patch to Airtable & Client
            table.patch(patch);
            client.put(updated);
        } else {
            if (updated == null) {
                logger.warn("Failed to Parse Tag, {}", record.getField("name").asText());
                return;
            }
            // Create New
            Tag posted = client.post(updated);
            AirtableRecord patch = new AirtableRecord();
            patch.setId(record.getId());
            patch.putField("tagId", posted.getTagId());
            patch.putFieldDate("updatedMillis", posted.getUpdatedMillis());
            patch.putFieldDate("createdMillis", posted.getCreatedMillis());
            table.patch(patch);
        }
    }

    private Tag.Counts getCount(Tag tag) {
        ObjectNode root = JsonUtils.createObjectNode();
        ObjectNode bool = JsonUtils.createObjectNode();
        bool.set("must", ElasticUtils.mustMatchAll());
        bool.set("filter", JsonUtils.createArrayNode()
                .add(ElasticUtils.filterTerm("dataType", "Place"))
                .add(ElasticUtils.filterTerm("tags.tagId", tag.getTagId()))
        );
        root.putObject("query").set("bool", bool);

        // Count
        Tag.Counts count = new Tag.Counts();
        count.setTotal(Optional.ofNullable(elasticClient.count(root)).orElse(0L));
        return count;
    }

    @Override
    protected Tag parse(AirtableRecord record) {
        Tag tag = new Tag();
        String type = record.getField("type").asText();
        if (type == null) return null;
        tag.setType(Tag.Type.valueOf(type));

        tag.setTagId(StringUtils.trimToNull(record.getField("tagId").asText()));
        tag.setName(StringUtils.trimToNull(record.getField("name").asText()));
        if (tag.getName() == null) return null;

        tag.setNames(new HashSet<>());
        tag.getNames().addAll(record.getFieldList("names", String.class));
        tag.getNames().add(tag.getName());

        tag.setPlace(new Tag.Place());
        String level = record.getField("place.level").asText();

        tag.getPlace().setLevel(StringUtils.isBlank(level) ? null : Integer.parseInt(level));
        tag.getPlace().setOrder(record.getField("place.order").asDouble());
        tag.getPlace().setRemapping(ImmutableSet.copyOf(record.getFieldList("place.remapping", String.class)));

        tag.setSearch(new Tag.Search());
        tag.getSearch().setEnabled(record.getField("search.enabled").asBoolean());
        tag.getSearch().setListed(record.getField("search.listed").asBoolean());
        return tag;
    }
}
