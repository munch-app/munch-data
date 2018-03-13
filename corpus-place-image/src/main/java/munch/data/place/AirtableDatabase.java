package munch.data.place;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableRecord;
import corpus.data.CorpusData;
import corpus.images.ImageField;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by: Fuxing
 * Date: 13/3/18
 * Time: 7:43 PM
 * Project: munch-data
 */
@Singleton
public final class AirtableDatabase {
    private final AirtableApi.Table table;

    private final Supplier<Map<String, Source>> supplier = Suppliers
            .memoizeWithExpiration(this::loadAll, 4, TimeUnit.HOURS);

    @Inject
    public AirtableDatabase(AirtableApi airtableApi) {
        this.table = airtableApi.base("app9HwzXIDflMdwVR").table("Image");
        // Load all
    }

    private Map<String, Source> loadAll() {
        Map<String, Source> sourceMap = new HashMap<>();
        table.select(Duration.ofSeconds(1)).forEachRemaining(record -> {
            String key = record.getField("UniqueId").asText();
            Source source = new Source(record);
            sourceMap.put(key, source);
        });
        return sourceMap;
    }

    public boolean allow(String source, String sourceId) {
        Source sourced = supplier.get().get(Source.createKey(source, sourceId));
        if (sourced == null) return true;

        return sourced.allow;
    }

    public static class Source {
        String source;
        String sourceId;
        String sourceName;
        boolean allow;

        long firstImageCount;
        long totalImageCount;

        public Source(CorpusData.Field imageField) {
            Map<String, String> metadata = imageField.getMetadata();
            if (metadata == null) return;

            this.source = metadata.get(ImageField.META_SOURCE);
            this.sourceId = metadata.get(ImageField.META_SOURCE_ID);
            this.sourceName = metadata.get(ImageField.META_SOURCE_NAME);
        }

        public Source(AirtableRecord record) {
            this.source = record.getField("Source").asText();
            this.sourceId = record.getField("Source Id").asText();
            this.sourceName = record.getField("Source Name").asText();

            this.allow = record.getField("Status").asText("").equalsIgnoreCase("allow");

            this.firstImageCount = record.getField("First Image Count").asLong();
            this.totalImageCount = record.getField("Total Image Count").asLong();
        }

        public static String createKey(String source, String sourceId) {
            return source + "|" + sourceId;
        }

        public static String createKey(CorpusData.Field field) {
            Map<String, String> metadata = field.getMetadata();
            if (metadata == null) return "";

            String source = metadata.getOrDefault(ImageField.META_SOURCE, "");
            String sourceId = metadata.getOrDefault(ImageField.META_SOURCE_ID, "");
            return createKey(source, sourceId);
        }

    }
}
