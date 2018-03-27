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
 * Date: 28/3/2018
 * Time: 12:36 AM
 * Project: munch-data
 */
@Singleton
public final class StreetSuffixDatabase {

    private final Set<String> names;

    @Inject
    public StreetSuffixDatabase() throws IOException {
        URL url = Resources.getResource("street-suffix.txt");
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
