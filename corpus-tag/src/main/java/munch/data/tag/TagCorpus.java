package munch.data.tag;

import catalyst.utils.exception.ExceptionRetriable;
import catalyst.utils.exception.Retriable;
import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.field.FieldUtils;
import munch.data.clients.TagClient;
import munch.data.exceptions.ClusterBlockException;
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
    private static final Retriable retriable = new ExceptionRetriable(20, Duration.ofMinutes(3), ClusterBlockException.class);
    private static final long dataVersion = 21;

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
        CorpusData placeTag = getPlaceTag(data);

        retriable.loop(() -> process(placeTag, data));

        // Sleep for 1 second every 5 processed
        sleep(200);
    }

    protected void process(CorpusData placeTag, CorpusData data) {
        if (placeTag != null) {
            if (!TagKey.updatedDate.equal(data, placeTag.getUpdatedDate(), dataVersion)) {
                data.replace(TagKey.updatedDate, placeTag.getUpdatedDate().getTime() + dataVersion);

                retriable.loop(() -> {
                    tagClient.put(createTag(placeTag));
                    corpusClient.put(corpusName, data.getCorpusKey(), data);
                    counter.increment("Updated");
                });
            }
        } else {
            retriable.loop(() -> {
                // To delete
                tagClient.delete(data.getCorpusKey());
                corpusClient.delete(corpusName, data.getCorpusKey());
                counter.increment("Deleted");
            });
        }
    }

    /**
     * @param data local persisted tracker
     * @return actual linked data
     */
    private CorpusData getPlaceTag(CorpusData data) {
        List<CorpusData> dataList = catalystClient.listCorpus(data.getCatalystId(),
                "Sg.MunchSheet.PlaceTag", 1, null, null);

        if (dataList.isEmpty()) return null;
        return dataList.get(0);
    }

    private Tag createTag(CorpusData data) {
        Tag tag = new Tag();
        tag.setId(data.getCorpusKey());
        tag.setName(FieldUtils.getValueOrThrow(data, "PlaceTag.name"));
        return tag;
    }
}
