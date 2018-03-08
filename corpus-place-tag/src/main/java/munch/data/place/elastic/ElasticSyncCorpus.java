package munch.data.place.elastic;

import corpus.data.CorpusData;
import corpus.engine.CatalystEngine;
import munch.data.clients.TagClient;
import munch.data.place.group.PlaceTagDatabase;
import munch.data.place.group.PlaceTagGroup;
import munch.data.structure.Tag;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;

/**
 * Created by: Fuxing
 * Date: 8/3/18
 * Time: 9:08 PM
 * Project: munch-data
 */
@Singleton
public final class ElasticSyncCorpus extends CatalystEngine<PlaceTagGroup> {
    private static final Logger logger = LoggerFactory.getLogger(ElasticSyncCorpus.class);

    private final PlaceTagDatabase database;
    private final TagClient tagClient;

    @Inject
    public ElasticSyncCorpus(PlaceTagDatabase database, TagClient tagClient) {
        super(logger);
        this.database = database;
        this.tagClient = tagClient;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(12);
    }

    @Override
    protected Iterator<PlaceTagGroup> fetch(long cycleNo) {
        return database.getAll().iterator();
    }

    @Override
    protected void process(long cycleNo, PlaceTagGroup data, long processed) {
        if (StringUtils.isAnyBlank(data.getRecordId(), data.getType(), data.getName())) return;

        Tag tag = new Tag();
        tag.setId(data.getRecordId());
        tag.setType(data.getType());
        tag.setName(data.getName());
        tag.setConverts(data.getConverts());
        tagClient.put(tag);

        // Put created into Elastic
        CorpusData corpusData = new CorpusData(cycleNo);
        corpusData.put("Tag.Indexed.name", data.getName());
        corpusData.put("Tag.Indexed.type", data.getType());
        corpusClient.put("Sg.Munch.Tag.Indexed", data.getRecordId(), corpusData);

        counter.increment("Indexed");
        sleep(10);
    }

    @Override
    protected void deleteCycle(long cycleNo) {
        corpusClient.listBefore("Sg.Munch.Tag.Indexed", cycleNo).forEachRemaining(data -> {
            tagClient.delete(data.getCorpusKey());
            corpusClient.delete("Sg.Munch.Tag.Indexed", data.getCorpusKey());
        });
        super.deleteCycle(cycleNo);
    }
}
