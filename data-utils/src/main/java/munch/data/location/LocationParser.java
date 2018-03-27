package munch.data.location;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by: Fuxing
 * Date: 27/3/2018
 * Time: 1:38 AM
 * Project: munch-data
 */
@Singleton
public final class LocationParser {
    private final List<CityParser> cityParsers = List.of(
            new SingaporeCityParser()
    );

    private final StreetSuffixDatabase suffixDatabase;
    private final LibpostalParser libpostalParser;

    @Inject
    public LocationParser(StreetSuffixDatabase suffixDatabase, LibpostalParser libpostalParser) {
        this.suffixDatabase = suffixDatabase;
        this.libpostalParser = libpostalParser;
    }

    public LocationData parse(String text) {
        List<String> groups = constructGroups(text);
        // Try parse with explicit parser first
        for (CityParser cityParser : cityParsers) {
            Optional<LocationData> data = tryParse(groups, cityParser);
            if (data.isPresent()) return data.get();
        }

        // Try parse with libpostal
        return tryParse(groups, libpostalParser).orElse(null);
    }

    /**
     * @param text text to break up and construct likely address lines
     * @return List of likely address
     */
    public List<String> constructGroups(String text) {
        // TODO chop up parts into tokens & construct
        // Known Prefix: Address: Location: Direction:
        // Known Suffix:
        // Known Prefix & Suffix: \t\n & -|–|—|:|@|\|

        // Extract Known Parts & mark them
        // Unit Number, [Suffix], Postcode, Country, City, House Number

        // Find Parts to Focus On then move back and forward to finalize
        return List.of();
    }

    /**
     * @param groups to try parse
     * @param parser to use
     * @return Optional LocationData
     */
    public Optional<LocationData> tryParse(List<String> groups, CityParser parser) {
        return groups.stream()
                .map(parser::parse)
                .filter(Objects::nonNull)
                .max(Comparator.comparingDouble(LocationData::getAccuracy));
    }
}
