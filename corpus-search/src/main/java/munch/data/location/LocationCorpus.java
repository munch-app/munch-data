package munch.data.location;

import com.google.common.collect.Iterators;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableMapper;
import corpus.data.CorpusData;
import corpus.engine.CorpusEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;

/**
 * Created by: Fuxing
 * Date: 13/3/18
 * Time: 9:54 PM
 * Project: munch-data
 */
@Singleton
public final class LocationCorpus extends CorpusEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(LocationCorpus.class);

    private final AirtableMapper mapper;

    @Inject
    public LocationCorpus(AirtableApi airtableApi) {
        super(logger);
        AirtableApi.Table table = airtableApi.base("appbCCXympYqlVyvU").table("Polygon");
        this.mapper = new AirtableMapper(table);
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(12);
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return Iterators.transform(mapper.select(), record -> {
            CorpusData data = new CorpusData("Sg.Munch.Location.Polygon", record.getId(), cycleNo);
            data.setFields(record.getFields());
            return data;
        });
    }

    @Override
    protected void deleteCycle(long cycleNo) {
        deleteCycle("Sg.Munch.Location.Polygon", cycleNo);
    }
}
