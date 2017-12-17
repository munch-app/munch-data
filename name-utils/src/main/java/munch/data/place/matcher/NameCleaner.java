package munch.data.place.matcher;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created By: Fuxing Loh
 * Date: 8/10/2017
 * Time: 9:07 PM
 * Project: munch-data
 */
@Singleton
public final class NameCleaner {
    static final Pattern PATTERN_COMPANY = Pattern.compile("pte\\.? ?ltd\\.?");
    static final Pattern PATTERN_COUNTRY = Pattern.compile("singapore\\s?$");

    private final NameNormalizer nameNormalizer;
    private final LocationCleaner locationCleaner;

    @Inject
    public NameCleaner(NameNormalizer nameNormalizer, LocationCleaner locationCleaner) {
        this.nameNormalizer = nameNormalizer;
        this.locationCleaner = locationCleaner;
    }

    public String clean(String name) {
        name = name.toLowerCase();
        // Normalize first
        name = nameNormalizer.normalize(name);
        // Remove Country Postfix: Singapore
        name = PATTERN_COUNTRY.matcher(name).replaceAll("");
        // Check if name is allowed to be cleaned
        if (!cleanable(name)) {
            return NameNormalizer.trim(name);
        }

        // Remove Company Postfix: PTE LTD
        name = PATTERN_COMPANY.matcher(name).replaceAll("");
        // Remove trailing location name
        name = locationCleaner.clean(name);
        // Finally trim any whitespace
        return NameNormalizer.trim(name);
    }

    public String simpleClean(String name) {
        name = name.toLowerCase();
        // Normalize first
        name = nameNormalizer.normalize(name);
        return NameNormalizer.trim(name);
    }

    /**
     * Name cannot be clean if < 8 char long
     * Or contains any spacing
     *
     * @param name check if name can be cleaned
     * @return whether it can be cleaned
     */
    private boolean cleanable(String name) {
        if (name.length() < 9) return false;
        return name.contains(" ");
    }

    @Singleton
    public static class LocationCleaner {
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
}
