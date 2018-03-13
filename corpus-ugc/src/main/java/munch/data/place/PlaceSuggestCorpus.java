package munch.data.place;

import com.google.common.collect.Iterators;
import corpus.data.CorpusData;
import corpus.engine.CorpusEngine;
import corpus.field.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Created by: Fuxing
 * Date: 9/3/18
 * Time: 10:23 PM
 * Project: munch-data
 */
@Singleton
public final class PlaceSuggestCorpus extends CorpusEngine<CorpusData> {
    private static final Pattern PATTERN_UUID = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    private static final Logger logger = LoggerFactory.getLogger(PlaceSuggestCorpus.class);

    private final SuggestMapper suggestMapper;

    @Inject
    public PlaceSuggestCorpus(SuggestMapper suggestMapper) {
        super(logger);
        this.suggestMapper = suggestMapper;
    }

    @Override
    protected Duration cycleDelay() {
        return Duration.ofHours(15);
    }

    @Override
    protected Iterator<CorpusData> fetch(long cycleNo) {
        return Iterators.transform(suggestMapper.select(Duration.ofSeconds(5)), input -> {
            CorpusData data = new CorpusData("Sg.MunchUGC.PlaceSuggest", input.getId(), cycleNo);
            String placeId = FieldUtils.getValue(data, "Place.id");
            if (placeId != null && PATTERN_UUID.matcher(placeId).matches()) {
                data.setCatalystId(placeId);
            }

            data.setFields(input.getFields());

            if (!FieldUtils.has(data, "Suggest.endorsedBy")) return null;
            return data;
        });
    }

    @Override
    protected void deleteCycle(long cycleNo) {
        deleteCycle("Sg.MunchUGC.PlaceSuggest", cycleNo);
    }
}
