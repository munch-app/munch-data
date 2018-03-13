package munch.data.location;

import catalyst.utils.LatLngUtils;
import com.google.common.collect.Iterators;
import corpus.airtable.AirtableApi;
import corpus.airtable.AirtableMapper;
import corpus.data.CorpusData;
import corpus.engine.CorpusEngine;
import corpus.field.FieldUtils;
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
public final class LandmarkCorpus extends CorpusEngine<CorpusData> {
    private static final Logger logger = LoggerFactory.getLogger(LandmarkCorpus.class);

    private final AirtableMapper mapper;

    @Inject
    public LandmarkCorpus(AirtableApi airtableApi) {
        super(logger);
        AirtableApi.Table table = airtableApi.base("appbCCXympYqlVyvU").table("Landmark");
        this.mapper = new AirtableMapper(table);
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(12);
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return Iterators.transform(mapper.select(), record -> {
            CorpusData data = new CorpusData("Sg.Munch.Location.Landmark", record.getId(), cycleNo);
            data.setFields(record.getFields());

            if (!validate(data)) return null;
            return data;
        });
    }

    @Override
    protected void deleteCycle(long cycleNo) {
        deleteCycle("Sg.Munch.Location.Landmark", cycleNo);
    }

    private boolean validate(CorpusData data) {
        if (!FieldUtils.has(data, "Landmark.name")) return false;
        if (!FieldUtils.has(data, "Landmark.type")) return false;

        String latLng = FieldUtils.getValue(data, "Landmark.latLng");
        if (!LatLngUtils.isLatLng(latLng)) return false;

        return true;
    }
}
