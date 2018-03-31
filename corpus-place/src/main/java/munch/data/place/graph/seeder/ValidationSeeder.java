package munch.data.place.graph.seeder;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import corpus.data.CorpusData;
import munch.data.place.graph.PlaceTree;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 30/3/18
 * Time: 6:10 PM
 * Project: munch-data
 */
public final class ValidationSeeder implements Seeder {
    private final Set<String> postalSet;

    public ValidationSeeder() {
        URL url = Resources.getResource("location-blocked-postal.txt");
        try {
            this.postalSet = ImmutableSet.copyOf(Resources.readLines(url, Charset.forName("UTF-8")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Result trySeed(String placeId, PlaceTree placeTree) {
        Map<String, List<CorpusData.Field>> fieldsMap = placeTree.getFieldsMap();

        if (!validatePostal(fieldsMap)) return Result.Block;
        if (!validateName(fieldsMap)) return Result.Block;

        return Result.Proceed;
    }

    private boolean validatePostal(Map<String, List<CorpusData.Field>> fieldsMap) {
        List<CorpusData.Field> fields = fieldsMap.get("Place.Location.postal");
        // Postal not found
        if (fields == null) return false;

        // Postal is blocked
        return fields.stream()
                .map(CorpusData.Field::getValue)
                .noneMatch(postalSet::contains);
    }

    private boolean validateName(Map<String, List<CorpusData.Field>> fieldsMap) {
        List<CorpusData.Field> fields = fieldsMap.get("Place.name");
        // Postal not found
        if (fields == null) return false;
        return !fields.isEmpty();
    }
}
