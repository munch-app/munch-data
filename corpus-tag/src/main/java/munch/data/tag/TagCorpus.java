package munch.data.tag;

import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.field.FieldUtils;
import munch.data.clients.TagClient;
import munch.data.structure.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 10/10/2017
 * Time: 3:21 AM
 * Project: munch-data
 */
@Singleton
public class TagCorpus extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(TagCorpus.class);

    private final TagClient tagClient;

    @Inject
    public TagCorpus(TagClient tagClient) {
        super(logger);
        this.tagClient = tagClient;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofMinutes(30);
    }

    @Override
    protected long loadCycleNo() {
        return System.currentTimeMillis();
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return corpusClient.list(corpusName);
    }

    @Override
    protected void process(long cycleNo, CorpusData data, long processed) {
        List<CorpusData> dataList = catalystClient.listCorpus(data.getCatalystId(),
                "Sg.MunchSheet.PlaceTag", 1, null, null);
        if (!dataList.isEmpty()) {
            // To put if changed
            CorpusData placeTag = dataList.get(0);
            if (TagKey.updatedDate.equal(data, placeTag.getUpdatedDate())) {
                tagClient.put(createTag(placeTag));
            }
        } else {
            // To delete
            tagClient.delete(data.getCorpusKey());
            corpusClient.delete(corpusName, data.getCorpusKey());
        }

        // Sleep for 1 second every 5 processed
        if (processed % 5 == 0) {
            sleep(1000);
        }
    }

    private Tag createTag(CorpusData data) {
        Tag tag = new Tag();
        tag.setId(data.getCorpusKey());
        tag.setName(FieldUtils.getValueOrThrow(data, "PlaceTag.name"));
        return tag;
    }
}