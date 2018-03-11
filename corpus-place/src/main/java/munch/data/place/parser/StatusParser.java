package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;
import munch.data.structure.Place;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 12/2/2018
 * Time: 6:09 PM
 * Project: munch-data
 */
@Singleton
public final class StatusParser extends AbstractParser<StatusParser.Status> {
    public enum Status {
        Delete, Close, Open
    }

    @Override
    public Status parse(Place place, List<CorpusData> list) {
        Set<String> statusSet = collectValue(list, PlaceKey.status).stream()
                .map(StringUtils::lowerCase)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (statusSet.contains("delete")) return Status.Delete;
        if (statusSet.contains("close")) return Status.Close;
        if (place == null) return Status.Close;

        return Status.Open;
    }
}
