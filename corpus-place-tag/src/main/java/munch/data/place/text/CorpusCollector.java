package munch.data.place.text;

import corpus.data.CorpusData;
import corpus.field.PlaceKey;

import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 4:28 PM
 * Project: munch-data
 */
@Singleton
public final class CorpusCollector extends AbstractCollector {
    private static final Set<String> IGNORE_CORPUS = Set.of("Sg.Munch.PlaceTag", "Sg.Munch.Place");

    @Override
    public List<CollectedText> collect(String placeId, List<CorpusData> list) {
        return list.stream()
                // Not to collect from self
                .filter(data -> !IGNORE_CORPUS.contains(data.getCorpusKey()))
                .flatMap(data -> data.getFields().stream())
                .filter(field -> field.getKey().equals(PlaceKey.description.getKey()))
                .map(field -> mapField(field, CollectedText.From.Place))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
