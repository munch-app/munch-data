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
import java.util.stream.Collectors;

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
    public Result trySeed(PlaceTree placeTree) {
        Map<String, List<CorpusData.Field>> fieldsMap = placeTree.getFieldsMap();

        if (!validatePostal(fieldsMap.get("Place.Location.postal"))) return Result.Block;


        List<CorpusData.Field> statusFields = fieldsMap.get("Place.status");
        if (statusFields == null) return Result.Proceed;

        Set<String> statusSet = statusFields.stream()
                .map(field -> field.getValue().toLowerCase())
                .collect(Collectors.toSet());

        if (statusSet.contains("delete")) return Result.Block;
        if (statusSet.contains("deleted")) return Result.Block;

        // TODO decay tracking, so know when to change to block
        if (statusSet.contains("close")) return Result.Decayed;
        if (statusSet.contains("closed")) return Result.Decayed;

        return Result.Proceed;
    }

    private boolean validatePostal(List<CorpusData.Field> fields) {
        // Postal not found
        if (fields == null) return false;

        // Postal is blocked
        return fields.stream()
                .map(CorpusData.Field::getValue)
                .anyMatch(postalSet::contains);
    }
}
