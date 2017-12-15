package munch.data.place.collector;

import corpus.data.CorpusData;

import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 4:28 PM
 * Project: munch-data
 */
@Singleton
public final class CorpusCollector extends AbstractCollector {

    @Override
    public List<CollectedImage> collect(String placeId, List<CorpusData> list) {
        return list.stream()
                // Not to collect from self
                .filter(data -> !data.getCorpusName().equals("Sg.Munch.PlaceImage"))
                .flatMap(data -> data.getFields().stream())
                .filter(field -> field.getKey().equals("Place.image"))
                .map(field -> mapField(field, CollectedImage.From.Place))
                .collect(Collectors.toList());
    }
}
