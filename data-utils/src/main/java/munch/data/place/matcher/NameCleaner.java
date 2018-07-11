package munch.data.place.matcher;

import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.regex.Pattern;

/**
 * Created By: Fuxing Loh
 * Date: 8/10/2017
 * Time: 9:07 PM
 * Project: munch-data
 */
@Singleton
public final class NameCleaner {
    static final Pattern PATTERN_COMPANY = Pattern.compile("pte\\.? *ltd\\.?|private limited|llp");
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
        if (StringUtils.isBlank(name)) return null;

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

}
