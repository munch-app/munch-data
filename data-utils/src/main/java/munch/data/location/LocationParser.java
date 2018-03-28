package munch.data.location;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

/**
 * Created by: Fuxing
 * Date: 27/3/2018
 * Time: 1:38 AM
 * Project: munch-data
 */
@Singleton
public final class LocationParser {
    private final List<CityParser> cityParsers;

    private final AddressGrouping grouping;
    private final LibpostalParser libpostalParser;

    @Inject
    public LocationParser(AddressGrouping grouping, LibpostalParser libpostalParser, StreetSuffixDatabase suffixDatabase) {
        this.grouping = grouping;
        this.libpostalParser = libpostalParser;
        this.cityParsers = List.of(
                new SingaporeCityParser(suffixDatabase)
        );
    }

    public LocationData parse(String text) {
        Set<List<String>> groups = grouping.group(text);
        // Try parse with explicit parser first
        for (CityParser cityParser : cityParsers) {
            Optional<LocationData> data = tryParse(groups, cityParser);
            if (data.isPresent()) return data.get();
        }

        // Try parse with libpostal
        return tryParse(groups, libpostalParser).orElse(null);
    }

    /**
     * @param groups to try parse
     * @param parser to use
     * @return Optional LocationData
     */
    public Optional<LocationData> tryParse(Collection<List<String>> groups, CityParser parser) {
        return groups.stream()
                .map(parser::parse)
                .filter(Objects::nonNull)
                .max(Comparator.comparingDouble(LocationData::getAccuracy));
    }
}
