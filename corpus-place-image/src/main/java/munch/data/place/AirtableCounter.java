package munch.data.place;

import com.fasterxml.jackson.databind.JsonNode;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableRecord;
import corpus.airtable.AirtableReplaceSession;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.restful.core.JsonUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 13/3/2018
 * Time: 12:58 AM
 * Project: munch-data
 */
@Singleton
public final class AirtableCounter {

    private final AirtableApi.Table table;
    private Map<String, AirtableDatabase.Source> sourceMap = new HashMap<>();

    @Inject
    public AirtableCounter(AirtableApi airtableApi) {
        this.table = airtableApi.base("app9HwzXIDflMdwVR").table("Image");
    }

    public void add(CorpusData data) {
        List<CorpusData.Field> fields = PlaceKey.image.getAll(data);
        fields.forEach(this::put);
    }

    public void put(CorpusData.Field imageField) {
        AirtableDatabase.Source source = sourceMap.computeIfAbsent(AirtableDatabase.Source.createKey(imageField), s -> new AirtableDatabase.Source(imageField));

        if (imageField.getValue().equals("0")) {
            source.firstImageCount++;
        }
        source.totalImageCount++;
    }

    public void finish(Duration delay) {
        AirtableReplaceSession session = new AirtableReplaceSession(delay, table, "UniqueId");
        sourceMap.forEach((s, source) -> {
            HashMap<String, JsonNode> map = new HashMap<>();
            map.put("UniqueId", JsonUtils.toTree(s));
            map.put("Source", JsonUtils.toTree(source.source));
            map.put("Source Id", JsonUtils.toTree(source.sourceId));
            map.put("Source Name", JsonUtils.toTree(source.sourceName));
            map.put("First Image Count", JsonUtils.toTree(source.firstImageCount));
            map.put("Total Image Count", JsonUtils.toTree(source.totalImageCount));

            AirtableRecord record = new AirtableRecord();
            record.setFields(map);

            session.put(record, table::patch, post -> {
                post.getFields().put("Status", JsonUtils.toTree("Allow"));
                table.post(post);
            });
        });
        session.close();

        sourceMap = new HashMap<>();
    }
}
