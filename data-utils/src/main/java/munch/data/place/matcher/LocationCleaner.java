package munch.data.place.matcher;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import munch.data.utils.PatternSplit;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 7/3/2018
 * Time: 11:23 PM
 * Project: munch-data
 */
@Singleton
public class LocationCleaner {
    static final PatternSplit PATTERN_LOCATION_JOINER = PatternSplit.compile("\\s(-|–|—|@|at)\\s");

    private final Set<String> locationsNames;

    /**
     * See resources/locations-cleanser.txt for the file
     * resource file must contains lowercase location names for cleaning
     *
     * @throws IOException error loading file
     */
    @Inject
    public LocationCleaner() throws IOException {
        URL url = Resources.getResource("location-cleanser.txt");
        this.locationsNames = ImmutableSet.copyOf(Resources.readLines(url, Charset.forName("UTF-8")));
    }

    /**
     * @param name name to clean, must be lower case
     * @return removed trailing location name in the name
     */
    public String clean(String name) {
        List<String> splits = PATTERN_LOCATION_JOINER.split(name, 0);
        if (splits.size() < 3) return name;

        // 2nd last will be delimiter and last will be location name
        String locationName = splits.get(splits.size() - 1);
        if (locationsNames.contains(locationName.toLowerCase())) {
            // If true, means last is location name
            // Remove last 2 splits
            splits.subList(splits.size() - 2, splits.size()).clear();

            // Join remaining splits together
            if (splits.size() == 1) return splits.get(0);
            return Joiner.on("").join(splits);
        }
        return name;
    }
}
