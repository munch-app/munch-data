package munch.data.location;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
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

    private final LibpostalParser libpostalParser;

    @Inject
    public LocationParser(LibpostalParser libpostalParser) {
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

    public List<String> constructGroups(String text) {
        // TODO Chop up parts into tokens
        // construct likely address lines
        // check how likely token contains address
        return null;
    }

    public Optional<LocationData> tryParse(List<String> groups, CityParser cityParser) {
        // TODO sort most accurate, return if found any
        return Optional.empty();
    }
}
