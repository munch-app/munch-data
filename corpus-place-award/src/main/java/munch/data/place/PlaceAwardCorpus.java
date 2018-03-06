package munch.data.place;

import com.google.common.collect.Iterators;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableMapper;
import corpus.airtable.AirtableRecord;
import corpus.airtable.ParsedRecord;
import corpus.airtable.field.DefaultMapper;
import corpus.airtable.field.LinkedRecordMapper;
import corpus.airtable.field.RenameMapper;
import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import corpus.field.PlaceKey;
import munch.collections.CollectionPlaceClient;
import munch.data.extended.ExtendedDataSync;
import munch.data.extended.PlaceAward;
import munch.data.extended.PlaceAwardClient;
import munch.data.location.PostalParser;
import munch.restful.core.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 6/3/18
 * Time: 3:22 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceAwardCorpus extends CatalystEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(PlaceAwardCorpus.class);

    private final PlaceAwardClient placeAwardClient;
    private final ExtendedDataSync<PlaceAward> dataSync;

    private final AirtableMapper awardedPlaceMapper;
    private final AirtableApi.Table awardPlaceTable;

    @Inject
    public PlaceAwardCorpus(AirtableApi airtableApi, CollectionPlaceClient collectionPlaceClient, PlaceAwardClient placeAwardClient) {
        super(logger);
        this.placeAwardClient = placeAwardClient;
        this.dataSync = new ExtendedDataSync<>(Duration.ofSeconds(1), placeAwardClient) {
            @Override
            protected void put(String placeId, PlaceAward data) {
                super.put(placeId, data);
                collectionPlaceClient.add(data.getUserId(), data.getCollectionId(), placeId);
            }

            @Override
            protected void delete(String placeId, PlaceAward data) {
                super.delete(placeId, data);
                collectionPlaceClient.remove(data.getUserId(), data.getCollectionId(), placeId);
            }
        };

        AirtableApi.Base base = airtableApi.base("appyZ1jrMU3w0G53V");
        AirtableApi.Table awardListTable = base.table("Award List");
        this.awardPlaceTable = base.table("Awarded Place");
        this.awardedPlaceMapper = new AirtableMapper(base.table("Awarded Place"), Map.of(
                "Place.name (Others)", RenameMapper.to("Place.name", DefaultMapper.INSTANCE),
                "Awards", new LinkedRecordMapper.CachedSleep((name, record) -> {
                    String awardName = record.getField("Award Name").asText();
                    String userId = record.getField("User Id").asText();
                    String collectionId = record.getField("Collection Id").asText();

                    return List.of(
                            PlaceAwardKey.awardName.createField(awardName,
                                    "UserId", userId,
                                    "CollectionId", collectionId)
                    );
                }, awardListTable, Duration.ofSeconds(2)),
                "Sort", RenameMapper.to("PlaceAward.sort", DefaultMapper.INSTANCE),
                "Place.id", RenameMapper.to("PlaceAward.placeId", DefaultMapper.INSTANCE),
                "Status", RenameMapper.to("PlaceAward.status", DefaultMapper.INSTANCE)
        ));
    }

    @Override
    protected boolean preCycle(long cycleNo) {
        // To always let AwardListCorpus run first
        sleep(Duration.ofSeconds(60));
        return super.preCycle(cycleNo);
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(18);
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        Iterator<ParsedRecord> iterator = awardedPlaceMapper.select(Duration.ofSeconds(2));
        return Iterators.transform(iterator, record -> {
            CorpusData data = new CorpusData("Sg.Munch.PlaceAward", record.getId(), cycleNo);
            data.setFields(record.getFields());
            return data;
        });
    }

    @Override
    protected void process(long cycleNo, CorpusData data, long processed) {
        if (!PlaceKey.name.has(data)) return;

        String address = PlaceKey.Location.address.getValue(data);
        if (address == null) return;

        String postal = PostalParser.parse(address);
        if (postal == null) {
            AirtableRecord record = new AirtableRecord();
            record.setId(data.getCorpusKey());
            record.setFields(Map.of(
                    "Status", JsonUtils.toTree("Postal Missing"),
                    "Place.id", JsonUtils.toTree("")
            ));
            awardPlaceTable.patch(record);
            sleep(3000);
            return;
        }

        // Apply Postal
        data.put(PlaceKey.Location.postal, postal);

        // Check PlaceId
        String placeId = getPlaceId(data);
        if (placeId == null) {
            // Not Linked
            AirtableRecord record = new AirtableRecord();
            record.setId(data.getCorpusKey());
            record.setFields(Map.of(
                    "Status", JsonUtils.toTree("Not Linked"),
                    "Place.id", JsonUtils.toTree("")
            ));
            awardPlaceTable.patch(record);
            corpusClient.put("Sg.Munch.PlaceAward", data.getCorpusKey(), data);
            sleep(3000);
            return;
        }


        if (PlaceAwardKey.placeId.has(data, placeId) &&
                PlaceAwardKey.status.has(data, "Linked")) return;

        // Apply Patch
        AirtableRecord record = new AirtableRecord();
        record.setId(data.getCorpusKey());
        record.setFields(Map.of(
                "Status", JsonUtils.toTree("Linked"),
                "Place.id", JsonUtils.toTree(placeId)
        ));
        awardPlaceTable.patch(record);
        corpusClient.put("Sg.Munch.PlaceAward", data.getCorpusKey(), data);
        sleep(3000);

        List<PlaceAward> awardList = PlaceAwardKey.awardName.getAll(data)
                .stream()
                .map(field -> toPlaceAward(data, field))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        dataSync.sync(data.getCorpusKey(), awardList);
    }

    private String getPlaceId(CorpusData data) {
        if (data.getCatalystId() == null) return null;

        if (!catalystClient.hasCorpus(data.getCatalystId(), "Sg.Munch.Place")) return null;
        return data.getCatalystId();
    }

    private PlaceAward toPlaceAward(CorpusData data, CorpusData.Field fieldData) {
        PlaceAward award = new PlaceAward();
        award.setUserId(fieldData.getMetadata().get("UserId"));
        award.setCollectionId(fieldData.getMetadata().get("CollectionId"));

        award.setSortKey(PlaceAwardKey.getSortKey(data));
        award.setAwardName(fieldData.getValue());

        // Validate all not null
        if (StringUtils.isAnyBlank(
                award.getUserId(), award.getCollectionId(),
                award.getSortKey(), award.getAwardName()
        )) return null;

        return award;
    }

    @Override
    protected void deleteCycle(long cycleNo) {
        corpusClient.listBefore("Sg.Munch.PlaceAward", cycleNo).forEachRemaining(data -> {
            // Remove All Awards
            placeAwardClient.delete(data.getCorpusKey());
        });
        super.deleteCycle(cycleNo);
    }
}
