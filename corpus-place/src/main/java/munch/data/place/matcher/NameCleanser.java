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
public final class NameCleanser {
    static final Pattern PATTERN_COMPANY = Pattern.compile("pte\\.? ?ltd\\.?");
    static final Pattern PATTERN_COUNTRY = Pattern.compile("singapore\\s?$");

    static final Pattern PATTERN_MULTI_SPACE = Pattern.compile(" {2,}");

    private final LocationCleanser locationCleanser;

    @Inject
    public NameCleanser(LocationCleanser locationCleanser) {
        this.locationCleanser = locationCleanser;
    }

    public String clean(String name) {
        name = name.toLowerCase();
        // Remove Company Postfix: PTE LTD
        name = PATTERN_COMPANY.matcher(name).replaceAll("");
        // Remove Country Postfix: Singapore
        name = PATTERN_COUNTRY.matcher(name).replaceAll("");
        // Remove multiple spaces "  " -> " "
        name = fixWhitespace(name);
        // Remove trailing location name
        name = locationCleanser.remove(name);
        // Finally trim any whitespace
        return name.trim();
    }

    static String fixWhitespace(String text) {
        return PATTERN_MULTI_SPACE.matcher(text).replaceAll(" ");
    }

    @Singleton
    public static class LocationCleanser {
        static final PatternSplit PATTERN_LOCATION_JOINER = PatternSplit.compile("\\s(-|–|—|@|at)\\s");

        private final Set<String> locationsNames;

        /**
         * See resources/locations-cleanser.txt for the file
         * resource file must contains lowercase location names for cleaning
         *
         * @throws IOException error loading file
         */
        @Inject
        public LocationCleanser() throws IOException {
            URL url = Resources.getResource("location-cleanser.txt");
            this.locationsNames = ImmutableSet.copyOf(Resources.readLines(url, Charset.forName("UTF-8")));
        }

        /**
         * @param name name to clean, must be lower case
         * @return removed trailing location name in the name
         */
        public String remove(String name) {
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
