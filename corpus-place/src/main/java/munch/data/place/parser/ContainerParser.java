package munch.data.place.parser;

import corpus.data.CorpusData;
import corpus.field.ContainerKey;
import munch.data.structure.Place;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 10/12/2017
 * Time: 12:28 PM
 * Project: munch-data
 */
@Singleton
public final class ContainerParser extends AbstractParser<List<Place.Container>> {

    @Override
    public List<Place.Container> parse(Place place, List<CorpusData> list) {
        List<CorpusData> containerPlaces = findAll(list, "Sg.Munch.ContainerPlace");

        return containerPlaces.stream().map(data -> {
            Place.Container container = new Place.Container();
            container.setId(ContainerKey.id.getValue(data));
            container.setName(ContainerKey.name.getValue(data));
            return container;
        })
                .filter(container -> StringUtils.isNoneBlank(container.getId(), container.getName()))
                .sorted(Comparator.comparing(Place.Container::getId))
                .collect(Collectors.toList());
    }
}