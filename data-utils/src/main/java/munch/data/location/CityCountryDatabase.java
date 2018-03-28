package munch.data.location;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 28/3/18
 * Time: 11:22 AM
 * Project: munch-data
 */
@Singleton
public final class CityCountryDatabase {
    private final Set<String> names;

    @Inject
    public CityCountryDatabase() throws IOException {
        URL url = Resources.getResource("city-country.txt");
        this.names = ImmutableSet.copyOf(Resources.readLines(url, Charset.forName("UTF-8")));
    }

    /**
     * @param name name to check
     * @return whether is it a suffix
     */
    public boolean is(String name) {
        if (StringUtils.isBlank(name)) return true;

        return names.contains(name.toLowerCase());
    }
}
