package munch.data.place;

import com.google.common.collect.Iterators;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableMapper;
import corpus.airtable.ParsedRecord;
import corpus.airtable.field.DefaultMapper;
import corpus.airtable.field.RenameMapper;
import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import munch.collections.CollectionClient;
import munch.collections.PlaceCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 6/3/18
 * Time: 4:57 PM
 * Project: munch-data
 */
@Singleton
public final class AwardListCorpus extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(AwardListCorpus.class);

    private final CollectionClient collectionClient;
    private final AirtableMapper awardListMapper;

    @Inject
    public AwardListCorpus(CollectionClient collectionClient, AirtableApi airtableApi) {
        super(logger);
        this.collectionClient = collectionClient;

        AirtableApi.Base base = airtableApi.base("appyZ1jrMU3w0G53V");
        this.awardListMapper = new AirtableMapper(base.table("Award List"), Map.of(
                "Award Name", RenameMapper.to("AwardList.awardName", DefaultMapper.INSTANCE),
                "User Id", RenameMapper.to("AwardList.userId", DefaultMapper.INSTANCE),
                "Collection Id", RenameMapper.to("AwardList.collectionId", DefaultMapper.INSTANCE)
        ), null);
    }


    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(18);
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        Iterator<ParsedRecord> iterator = awardListMapper.select(Duration.ofSeconds(2));
        return Iterators.transform(iterator, record -> {
            CorpusData data = new CorpusData("Sg.Munch.AwardList", record.getId(), cycleNo);
            data.setFields(record.getFields());
            return data;
        });
    }

    @Override
    protected void process(long cycleNo, CorpusData data, long processed) {
        corpusClient.put("Sg.Munch.AwardList", data.getCorpusKey(), data);

        PlaceCollection collection = new PlaceCollection();
        collection.setUserId(AwardListKey.userId.getValueOrThrow(data));
        collection.setCollectionId(AwardListKey.collectionId.getUUID(data));
        collection.setName(AwardListKey.awardName.getValueOrThrow(data));

        collection.setSortKey(AwardListKey.collectionId.getLong(data));
        collection.setPrivacy(PlaceCollection.PRIVACY_PUBLIC);

        // Put to collection db
        collectionClient.put(collection);
    }

    @Override
    protected void deleteCycle(long cycleNo) {
        super.deleteCycle(cycleNo);

        corpusClient.listBefore("Sg.Munch.AwardList", cycleNo).forEachRemaining(data -> {
            String userId = AwardListKey.userId.getValueOrThrow(data);
            String collectionId = AwardListKey.collectionId.getUUID(data);
            collectionClient.delete(userId, collectionId);
        });
    }
}
