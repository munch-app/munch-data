package munch.data.place.parser;

import corpus.data.CorpusData;
import munch.data.structure.Place;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/10/2017
 * Time: 10:12 PM
 * Project: munch-data
 */
@Singleton
public final class TagParser extends AbstractParser<Place.Tag> {

    /**
     * @param place read-only place data
     * @param list  list of corpus data to parse from
     * @return Place.Tag, tags must all be in lowercase
     */
    @Override
    public Place.Tag parse(Place place, List<CorpusData> list) {
        List<CorpusData.Field> fields = findFields(list);
        List<String> explicits = parseExplicits(fields);
        List<String> implicits = parseImplicits(fields);

        Place.Tag tag = new Place.Tag();
        tag.setExplicits(explicits);
        tag.setImplicits(implicits);
        return tag;
    }

    private List<String> parseExplicits(List<CorpusData.Field> fields) {
        List<String> explicits = fields.stream()
                .filter(field -> field.getKey().equals("Tag.explicits"))
                .sorted(Comparator.comparingInt(value -> Integer.parseInt(value.getMetadata().get("Sort"))))
                .map(v -> v.getValue().toLowerCase())
                .collect(Collectors.toList());

        // If no tags, restaurant is the default
        if (explicits.isEmpty()) return Collections.singletonList("restaurant");
        return explicits;
    }

    private List<String> parseImplicits(List<CorpusData.Field> fields) {
        return fields.stream()
                .filter(field -> field.getKey().equals("Tag.implicits"))
                .sorted(Comparator.comparingInt(value -> Integer.parseInt(value.getMetadata().get("Sort"))))
                .map(v -> v.getValue().toLowerCase())
                .collect(Collectors.toList());
    }

    public List<CorpusData.Field> findFields(List<CorpusData> list) {
        for (CorpusData data : list) {
            if (data.getCorpusName().equals("Sg.Munch.Place.Tag")) {
                return data.getFields();
            }
        }
        return List.of();
    }
}