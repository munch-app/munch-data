package munch.data.location.container;

import com.google.common.collect.Iterators;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableMapper;
import corpus.airtable.AirtableRecord;
import corpus.airtable.field.AttachmentMapper;
import corpus.airtable.field.ImageMapper;
import corpus.airtable.field.RenameMapper;
import corpus.data.CorpusData;
import corpus.engine.CorpusEngine;
import corpus.field.ContainerKey;
import corpus.images.ImageCachedClient;
import munch.data.clients.ContainerClient;
import munch.data.elastic.ElasticIndex;
import munch.restful.core.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Created by: Fuxing
 * Date: 13/3/18
 * Time: 9:54 PM
 * Project: munch-data
 */
@Singleton
public final class ContainerCorpus extends CorpusEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(ContainerCorpus.class);

    private final WKTReader wktReader = new WKTReader();

    private final AirtableMapper mapper;
    private final ElasticIndex elasticIndex;
    private final ContainerClient containerClient;
    private final AirtableApi.Table table;

    @Inject
    public ContainerCorpus(AirtableApi airtableApi, ElasticIndex elasticIndex, ContainerClient containerClient, ImageCachedClient imageClient) {
        super(logger);
        this.elasticIndex = elasticIndex;
        this.containerClient = containerClient;

        this.table = airtableApi.base("appbCCXympYqlVyvU").table("Container");
        this.mapper = new AirtableMapper(table, Map.of(
                "Container.images", RenameMapper.to("Container.image", new AttachmentMapper(imageClient)),
                "Container.image", new ImageMapper(imageClient)
        ));
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(12);
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return Iterators.transform(mapper.select(Duration.ofSeconds(1)), record -> {
            CorpusData data = new CorpusData("Sg.Munch.Location.Container", cycleNo);
            data.setFields(record.getFields());
            ContainerKey.type.get(data).ifPresent(field -> {
                switch (field.getValue()) {
                    case "Area":
                        data.put(ContainerKey.matching, "polygon");
                        break;

                    case "Hawker Centre":
                    case "Shopping Mall":
                        data.put(ContainerKey.matching, "postal");
                        break;
                }
            });

            ContainerKey.id.get(data).ifPresentOrElse(field -> {
                data.setCorpusKey(field.getValue());
            }, () -> {
                String containerId = UUID.randomUUID().toString().toLowerCase();

                AirtableRecord patchRecord = new AirtableRecord();
                patchRecord.setId(record.getId());
                patchRecord.setFields(Map.of("Container.id", JsonUtils.toTree(containerId)));
                table.patch(patchRecord);

                data.put(ContainerKey.id, containerId);
                data.setCorpusKey(containerId);
            });

            if (!validate(data)) return null;
            return data;
        });
    }

    @Override
    protected void process(long cycleNo, CorpusData data, long processed) {
        super.process(cycleNo, data, processed);
    }

    @Override
    protected void deleteCycle(long cycleNo) {
        corpusClient.listBefore("Sg.Munch.Location.Container", cycleNo).forEachRemaining(data -> {
            containerClient.delete(data.getCorpusKey());
            corpusClient.delete("Sg.Munch.Location.Container", data.getCorpusKey());
            counter.increment("Deleted");
        });
    }

    private boolean validate(CorpusData data) {
        if (!ContainerKey.id.has(data)) return false;
        if (!ContainerKey.name.has(data)) return false;
        if (!ContainerKey.type.has(data)) return false;
        if (!ContainerKey.matching.has(data)) return false;

        if (!ContainerKey.Location.latLng.has(data)) return false;
        if (!ContainerKey.Location.city.has(data)) return false;
        if (!ContainerKey.Location.country.has(data)) return false;

        if (ContainerKey.Location.polygon.has(data)) {
            try {
                wktReader.read(ContainerKey.Location.polygon.getValueOrThrow(data));
            } catch (ParseException e) {
                logger.warn("ParseException for Polygon", e);
                return false;
            }
        }

        return true;
    }
}
