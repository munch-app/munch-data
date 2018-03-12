package munch.data.place;

import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableRecord;
import corpus.airtable.AirtableReplaceSession;
import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import corpus.images.ImageField;
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
    private Map<String, Source> sourceMap = new HashMap<>();

    @Inject
    public AirtableCounter(AirtableApi airtableApi) {
        this.table = airtableApi.base("app9HwzXIDflMdwVR").table("Image");
    }

    public void add(CorpusData data) {
        List<CorpusData.Field> fields = PlaceKey.image.getAll(data);
        fields.forEach(this::put);
    }

    public void put(CorpusData.Field imageField) {
        Source source = sourceMap.computeIfAbsent(getKey(imageField), s -> new Source(imageField));

        if (imageField.getValue().equals("0")) {
            source.firstImageCount++;
        }
        source.totalImageCount++;
    }

    public void finish(Duration delay) {
        AirtableReplaceSession session = new AirtableReplaceSession(delay, table, "UniqueId");
        sourceMap.forEach((s, source) -> {
            AirtableRecord record = new AirtableRecord();
            record.setFields(Map.of(
                "UniqueId", JsonUtils.toTree(s),
                "Source", JsonUtils.toTree(source.source),
                "Source Id", JsonUtils.toTree(source.sourceId),
                "Source Name", JsonUtils.toTree(source.sourceName),
                "First Image Count", JsonUtils.toTree(source.firstImageCount),
                "Total Image Count", JsonUtils.toTree(source.totalImageCount)
            ));
            session.put(record);
        });
        session.close();

        sourceMap = new HashMap<>();
    }

    private String getKey(CorpusData.Field field) {
        Map<String, String> metadata = field.getMetadata();
        if (metadata == null) return "";

        String source = metadata.getOrDefault(ImageField.META_SOURCE, "");
        String sourceId = metadata.getOrDefault(ImageField.META_SOURCE_ID, "");
        return source + "_" + sourceId;
    }

    private class Source {
        private String source;
        private String sourceId;
        private String sourceName;

        private int firstImageCount;
        private int totalImageCount;

        public Source(CorpusData.Field imageField) {
            Map<String, String> metadata = imageField.getMetadata();
            if (metadata == null) return;

            this.source = metadata.get(ImageField.META_SOURCE);
            this.sourceId = metadata.get(ImageField.META_SOURCE_ID);
            this.sourceName = metadata.get(ImageField.META_SOURCE_NAME);
        }
    }
}
